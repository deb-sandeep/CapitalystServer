package com.sandy.capitalyst.server.breeze.internal;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;
import java.util.TimeZone ;
import java.util.TreeSet ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;

public abstract class BreezeAPI<T> {

    static final Logger log = Logger.getLogger( BreezeAPI.class ) ;
    
    private static final boolean PRINT_RESPONSE = true ;
    
    protected static SimpleDateFormat ISO_8601_FMT = new SimpleDateFormat( Breeze.ISO8601_FMT ) ;
    
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
    
    protected BreezeAPI( String apiURL, Class<T> entityClass ) {
        this.apiEndpointUrl = apiURL ;
        this.entityClass = entityClass ;
        this.endpointId = apiEndpointUrl.substring( Breeze.BRZ_API_BASEURL.length() ) ;
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
        
        log.debug( "\nExecuting BreezeAPI " + endpointId + "\n" ) ;
        
        checkMandatoryParameters() ;
        
        BreezeSession session = BreezeSessionManager.instance().getSession( cred ) ;
        if( session != null ) {
            
            String responseStr = netClient.get( apiEndpointUrl, params, session ) ;
            JsonNode json = jsonParser.readTree( responseStr ) ;
            
            if( PRINT_RESPONSE ) {
                log.debug( "API response:" ) ;
                log.debug( jsonParser.writeValueAsString( json ) ) ;
            }
            
            response = createResponse( json ) ;
        }
        else {
            throw new IllegalStateException( "Active session for " + 
                                     cred.getUserName() + " does not exist." ) ;
        }
        return response ;
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
