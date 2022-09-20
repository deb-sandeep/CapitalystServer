package com.sandy.capitalyst.server.breeze.internal;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT ;

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

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener.APIInvocationInfo ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient.BreezeAPIException ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;
import com.sandy.common.util.StringUtil ;

public abstract class BreezeAPI<T> {

    static final Logger log = Logger.getLogger( BreezeAPI.class ) ;
    
    private static final boolean PRINT_RESPONSE = true ;
    private static final boolean PRINT_INVOCATION_LOG = false ;
    
    protected static SimpleDateFormat ISO_8601_FMT = 
                                    new SimpleDateFormat( Breeze.ISO8601_FMT ) ;
    
    protected static SimpleDateFormat ISO_8601_MILLIS_FMT 
                      = new SimpleDateFormat( Breeze.ISO8601_FMT_WITH_MILLIS ) ;
    
    static {
        ISO_8601_FMT.setTimeZone( TimeZone.getTimeZone( "GMT" ) ) ;
    }

    private String apiEndpointUrl = null ;
    private String endpointId = null ;
    private BreezeNetworkClient netClient = null ;
    private ObjectMapper jsonParser = null ;
    private Class<T> entityClass = null ;
    
    private Set<String> mandatoryParameters = new TreeSet<>() ;
    protected Map<String, String> params = new HashMap<>() ;
    
    protected BreezeAPI( String apiId, String apiURL, Class<T> entityClass ) {
        this.endpointId = apiId ;
        this.apiEndpointUrl = apiURL ;
        this.entityClass = entityClass ;
        this.netClient = BreezeNetworkClient.instance() ;
        this.jsonParser = new ObjectMapper().enable( INDENT_OUTPUT ) ;
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
    
    public BreezeAPIResponse<T> execute( BreezeCred cred ) 
            throws Exception {
        
        BreezeAPIResponse<T> response = null ;
        
        if( PRINT_INVOCATION_LOG ) {
            log.debug( "Executing BreezeAPI " + endpointId + 
                    " for " + cred.getUserName() ) ;
        }
        
        checkMandatoryParameters() ;
        
        BreezeSession session = BreezeSessionManager.instance().getSession( cred ) ;
        APIInvocationInfo invInfo = null ;

        if( session != null ) {
            
            invInfo = createInvocationInfo( cred ) ;
            
            notifyListeners( "preBreezeCall", invInfo ) ;
            
            long startTime = System.currentTimeMillis() ;
            try {
                String responseStr = null ;
                responseStr = netClient.get( apiEndpointUrl, params, session ) ;
                
                JsonNode json = jsonParser.readTree( responseStr ) ;
                
                if( PRINT_RESPONSE ) {
                    log.debug( "API response:" ) ;
                    log.debug( jsonParser.writeValueAsString( json ) ) ;
                }
                
                response = createResponse( json ) ;
                invInfo.setCallStatus( response.getStatus() ) ;
            }
            catch( BreezeAPIException e ) {
                
                log.error( "Breeze API error. Status = " + e.getStatus() + 
                           ". Msg = " + e.getErrorMsg() ) ;
                
                invInfo.setCallStatus( e.getStatus() ) ;
                invInfo.setErrorMsg( e.getErrorMsg() ) ;
                
                response = new BreezeAPIResponse<>() ;
                response.setStatus( e.getStatus() ) ;
                response.setError( e.getErrorMsg() ) ;
            }
            finally {
                if( response != null ) {
                    response.setCredential( cred ) ;
                }
                
                long endTime = System.currentTimeMillis() ;
                int timeTaken = (int)(endTime - startTime) ;
                
                invInfo.setCallDurationInMillis( timeTaken ) ;
                if( PRINT_INVOCATION_LOG ) {
                    log.debug( "  Status = " + invInfo.getCallStatus() + 
                               ". Latency = " + timeTaken + " ms." ) ;
                }

                notifyListeners( "postBreezeCall", invInfo ) ;
            }
        }
        else {
            throw new IllegalStateException( "Active session for " + 
                                     cred.getUserName() + " does not exist." ) ;
        }
        return response ;
    }
    
    private void notifyListeners( String callbackName, 
                                  APIInvocationInfo info ) 
        throws Exception {
        
        List<BreezeAPIInvocationListener> listeners = null ;
        Method callbackMethod = BreezeAPIInvocationListener.class.getMethod( callbackName, APIInvocationInfo.class ) ;
        
        listeners = Breeze.instance().getListeners() ;
        for( BreezeAPIInvocationListener l : listeners ) {
            callbackMethod.invoke( l, info ) ;
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
    
    private void checkMandatoryParameters() {
        if( !mandatoryParameters.isEmpty() ) {
            for( String param : mandatoryParameters ) {
                if( !params.containsKey( param ) ) {
                    throw new IllegalStateException( 
                            "Mandatory parameter " + param + " missing." ) ;
                }
            }
        }
    }
    
    public BreezeAPIResponse<T> createResponse( JsonNode rootNode ) 
        throws Exception {
        
        @SuppressWarnings( "unchecked" )
        BreezeAPIResponse<T> response = BreezeAPIResponse.class.getConstructor().newInstance() ;
        
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
        
        return response ;
    }
}
