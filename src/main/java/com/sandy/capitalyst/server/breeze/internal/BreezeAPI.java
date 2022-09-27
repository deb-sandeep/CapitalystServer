package com.sandy.capitalyst.server.breeze.internal;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT ;

import java.io.IOException ;
import java.lang.reflect.InvocationTargetException ;
import java.lang.reflect.Method ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.TimeZone ;
import java.util.TreeSet ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener.APIInvocationInfo ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.breeze.BreezeException.Type ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;
import com.sandy.common.util.StringUtil ;

public abstract class BreezeAPI<T> {

    public static final Logger log = Logger.getLogger( BreezeAPI.class ) ;
    
    private static final boolean PRINT_INVOCATION_LOG = false ;
    
    protected static SimpleDateFormat ISO_8601_FMT = 
                                    new SimpleDateFormat( Breeze.ISO8601_FMT ) ;
    
    protected static SimpleDateFormat ISO_8601_MILLIS_FMT 
                      = new SimpleDateFormat( Breeze.ISO8601_FMT_WITH_MILLIS ) ;
    
    static {
        ISO_8601_FMT.setTimeZone( TimeZone.getTimeZone( "GMT" ) ) ;
    }

    private BreezeNVPConfig     breezeCfg      = null ;
    private BreezeNetworkClient netClient      = null ;
    private String              endpointId     = null ;
    private ObjectMapper        jsonParser     = null ;
    private Class<T>            entityClass    = null ;
    private String              apiEndpointUrl = null ;
    
    private Set<String> mandatoryParameters = new TreeSet<>() ;
    protected Map<String, String> params = new HashMap<>() ;
    
    protected BreezeAPI( String apiId, String apiURL, Class<T> entityClass ) {
        
        this.endpointId     = apiId ;
        this.apiEndpointUrl = apiURL ;
        this.entityClass    = entityClass ;
        this.netClient      = BreezeNetworkClient.instance() ;
        this.jsonParser     = new ObjectMapper().enable( INDENT_OUTPUT ) ;
        this.breezeCfg      = Breeze.instance().getNVPCfg() ;
    }
    
    public void clearParams() {
        this.params.clear() ;
    }
    
    public void addParam( String key, String value ) {
        this.params.put( key, value ) ;
    }
    
    public void addParam( String key, Date date ) {
        this.params.put( key, ISO_8601_FMT.format( date ) ) ;
    }
    
    public void addMandatoryParameter( String... parameters ) {
        if( parameters != null ) {
            for( String p : parameters ) {
                mandatoryParameters.add( p ) ;
            }
        }
    }
    
    public final BreezeAPIResponse<T> execute( BreezeCred cred ) 
            throws BreezeException {
        
        BreezeSession        session  = null ;
        APIInvocationInfo    invInfo  = null ;
        BreezeAPIResponse<T> response = null ;
        
        BreezeNVPConfig cfg = Breeze.config() ;
        
        if( cfg.isPrintAPICallLog() ) {
            log.debug( "Executing BreezeAPI " + endpointId + 
                       " for " + cred.getUserName() ) ;
        }
        
        checkMandatoryParameters() ;
        
        session = BreezeSessionManager.instance().getSession( cred ) ;
        
        if( session == null ) {
            throw BreezeException.sessionError( cred.getUserName(),
                                                "Session for API call",
                                                "Session could not be obtained." ) ;
        }
        else if( session.isInitializationRequired() ) {
            throw BreezeException.sessionError( cred.getUserName(),
                                                "Session for API call",
                                                "Session is not active." ) ;
        }
        else if( session.isDayLimitReached() ) {
            
            throw BreezeException.dayRateExceed( cred.getUserName() ) ;
        }
        else {
            
            long startTime = System.currentTimeMillis() ;

            invInfo = createInvocationInfo( cred ) ;
            notifyListeners( "preBreezeCall", invInfo ) ;
            
            try {
                try {
                    String responseStr = null ;
                    
                    responseStr = netClient.get( apiEndpointUrl, params, session ) ;
                    
                    JsonNode json = jsonParser.readTree( responseStr ) ;
                    if( breezeCfg.isPrintAPIResponse() ) {
                        log.debug( "API response:" ) ;
                        log.debug( jsonParser.writeValueAsString( json ) ) ;
                    }
                    
                    response = createResponse( json ) ;
                    
                    if( response.getStatus() == 500 ) {
                        throw BreezeException.serverError( responseStr ) ;
                    }
                }
                catch( IOException e ) {
                    throw BreezeException.appException( "JSON Processing error", e ) ;
                }
            }
            catch( BreezeException e ) {
                
                log.error( "Exception encountered : " + e ) ;
                
                invInfo.setCallStatus( e.getHttpStatusCode() ) ;
                invInfo.setErrorMsg(   e.getMessage() ) ;
                
                BreezeSessionManager sessionMgr = BreezeSessionManager.instance() ;
                
                if( e.getType() == Type.API_DAY_LIMIT_EXCEED ) {
                    sessionMgr.setDayLimitReached( cred ) ;
                }
                else if( e.getType() == Type.SESSION_ERROR ) {
                    sessionMgr.invalidateSession( cred ) ;
                }
                
                throw e ;
            }
            finally {
                
                long endTime = System.currentTimeMillis() ;
                int timeTaken = (int)(endTime - startTime) ;

                if( response != null ) {
                    invInfo.setCallStatus( response.getStatus() ) ;
                    response.setCredential( cred ) ;
                    response.setTimeTakenInMillis( timeTaken ) ;
                }
                
                invInfo.setCallDurationInMillis( timeTaken ) ;
                
                if( PRINT_INVOCATION_LOG ) {
                    log.debug( "  Status = " + invInfo.getCallStatus() + 
                               ". Latency = " + timeTaken + " ms." ) ;
                }

                notifyListeners( "postBreezeCall", invInfo ) ;
            }
        }
        
        return response ;
    }
    
    private void notifyListeners( String callbackName, 
                                  APIInvocationInfo info ) {
        
        try {
            List<BreezeAPIInvocationListener> listeners = null ;
            Method callbackMethod = null ;
            
            callbackMethod = BreezeAPIInvocationListener.class.getMethod( 
                                       callbackName, APIInvocationInfo.class ) ;
            
            listeners = Breeze.instance().getListeners() ;
            
            for( BreezeAPIInvocationListener l : listeners ) {
                callbackMethod.invoke( l, info ) ;
            }
        }
        catch( Exception e ) {
            log.error( "This should never have happened", e ) ;
        }
    }
    
    private APIInvocationInfo createInvocationInfo( BreezeCred cred ) {
        
        Date now = new Date() ;
        APIInvocationInfo info = new APIInvocationInfo() ;
        
        info.setInvocationId( generateInvocationId( cred, now ) ) ;
        info.setApiId( this.endpointId ) ;
        info.setCallDatetime( new Date() ) ;
        info.setUserName( cred.getUserName() ) ;
        
        return info ;
    }
    
    private String generateInvocationId( BreezeCred cred, Date time ) {
        
        StringBuilder sb = new StringBuilder() ;
        
        sb.append( ( ISO_8601_MILLIS_FMT.format( time ) ) )
          .append( cred.getUserName() )
          .append( this.endpointId ) ;
        
        String hash = StringUtil.getHash( sb.toString() ) ;
        return hash ;
    }
    
    private void checkMandatoryParameters() throws BreezeException {
        
        if( !mandatoryParameters.isEmpty() ) {
            for( String param : mandatoryParameters ) {
                if( !params.containsKey( param ) ) {
                    throw BreezeException.appException( 
                            "Mandatory parameter " + param + " missing." ) ;
                }
            }
        }
    }
    
    @SuppressWarnings( "unchecked" )
    public BreezeAPIResponse<T> createResponse( JsonNode rootNode ) 
        throws BreezeException {
        
        BreezeAPIResponse<T> response = null ;
        
        try {
            response = BreezeAPIResponse.class.getConstructor().newInstance() ;
            
            int      statusCode  = rootNode.get( "Status" ).asInt() ;
            String   errorMsg    = rootNode.get( "Error" ).asText() ;
            JsonNode resultsNode = rootNode.get( "Success" ) ;
            
            response.setStatus( statusCode ) ;
            response.setError( errorMsg ) ;
            
            if( resultsNode != null ) {
                
                int numChildren = resultsNode.size() ;
                for( int i=0; i<numChildren; i++ ) {
                    
                    JsonNode entityNode = resultsNode.get( i ) ;
                    T t = jsonParser.treeToValue( entityNode, entityClass ) ;
                    response.addEntity( t ) ;
                }
            }
        }
        catch( IllegalAccessException    | 
               IllegalArgumentException  | 
               InvocationTargetException | 
               NoSuchMethodException     |
               SecurityException         | 
               InstantiationException e ) {

            log.error( "This should never have happened", e ) ;
        }
        catch( JsonProcessingException e ) {
            throw BreezeException.appException( "Response JSON invalid.", e ) ;
        }
        
        return response ;
    }
}
