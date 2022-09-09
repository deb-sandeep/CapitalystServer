package com.sandy.capitalyst.server.breeze.internal;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.TimeZone ;

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
    private ObjectMapper jsonPrinter = null ;
    
    protected Map<String, String> params = new HashMap<>() ;
    
    protected BreezeAPI( String apiURL ) {
        this.apiEndpointUrl = apiURL ;
        this.endpointId = apiEndpointUrl.substring( Breeze.BRZ_API_BASEURL.length() ) ;
        this.netClient = BreezeNetworkClient.instance() ;
        this.jsonPrinter = new ObjectMapper().enable( INDENT_OUTPUT ) ;
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
    
    public T execute( BreezeCred cred ) throws Exception {
        
        T response = null ;
        
        log.debug( "\nExecuting BreezeAPI " + endpointId + "\n" ) ;
        
        BreezeSession session = BreezeSessionManager.instance().getSession( cred ) ;
        if( session != null ) {
            String responseStr = netClient.get( apiEndpointUrl, params, session ) ;
            
            if( PRINT_RESPONSE ) {
                JsonNode json = jsonPrinter.readTree( responseStr ) ;
                log.debug( "API response:" ) ;
                log.debug( jsonPrinter.writeValueAsString( json ) ) ;
            }
            return response ;
        }
        else {
            throw new IllegalStateException( "Active session for " + 
                                     cred.getUserName() + " does not exist." ) ;
        }
    } 
}
