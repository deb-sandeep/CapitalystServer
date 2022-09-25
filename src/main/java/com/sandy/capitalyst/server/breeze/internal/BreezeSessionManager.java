package com.sandy.capitalyst.server.breeze.internal;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;
import java.net.URLEncoder ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.concurrent.TimeUnit ;

import org.apache.commons.codec.binary.Hex ;
import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.jsoup.Jsoup ;
import org.jsoup.nodes.Document ;
import org.jsoup.nodes.Element ;
import org.jsoup.select.Elements ;

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.core.util.RSACipher ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

import lombok.Data ;
import okhttp3.MediaType ;
import okhttp3.OkHttpClient ;
import okhttp3.Request ;
import okhttp3.RequestBody ;
import okhttp3.Response ;

public class BreezeSessionManager {

    private static final Logger log = Logger.getLogger( BreezeSessionManager.class ) ;
    
    private static final String API_USER_BASEURL = "https://api.icicidirect.com/apiuser" ;
    
    private static final MediaType FORM_DATA = MediaType.parse( "application/x-www-form-urlencoded" ) ;
    
    @Data
    public static class BreezeSession implements Serializable {
        
        private static final long serialVersionUID = -1427026265072334302L ;

        // If this flag is set, it implies that the session has been 
        // invalidated and the session would need to be initialized/created
        // again. 
        private boolean valid = false ;
        
        private String userId       = null ;
        private String sessionId    = null ;
        private String sessionToken = null ;
        private Date   creationTime = null ;
        
        private BreezeCred cred = null ;
        
        private boolean initializationRequired() {
            
            if( !this.valid ) {
                return true ;
            }
            else if( StringUtil.isEmptyOrNull( this.sessionToken ) ) {
                return true ;
            } 
            else {
                Date todayStart = new Date() ;
                todayStart = DateUtils.truncate( todayStart, Calendar.DAY_OF_MONTH ) ;
                if( this.creationTime.before( todayStart ) ) {
                    log.debug( "  New session id required. Old one expired." ) ;
                    return true ;
                }
            }
            return false ;
        }
    }
    
    private static final String FAKE_HDRS[][] = {
        { "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36" }
    } ;
    
    private static BreezeSessionManager instance = null ;
    
    private OkHttpClient client = null ;
    
    private Map<String, BreezeSession> sessionMap = new HashMap<>() ;
    
    public static BreezeSessionManager instance() {
        if( instance == null ) {
            instance = new BreezeSessionManager() ;
        }
        return instance ;
    }
    
    private BreezeSessionManager() {
        this.client = new OkHttpClient.Builder()
                        .connectTimeout( 60, TimeUnit.SECONDS )
                        .readTimeout( 60, TimeUnit.SECONDS )
                        .writeTimeout( 60, TimeUnit.SECONDS )
                        .build() ;
    }
    
    public void setCredentialsUpdated() {
        // Invalidate all the sessions
        for( BreezeSession session : sessionMap.values() ) {
            session.setValid( false ) ;
        }
    }
    
    /**
     * Returns a valid BreezeSession or null if a valid session could not 
     * be created.
     */
    public BreezeSession getSession( BreezeCred cred ) {
        
        String uid = cred.getUserId() ;
        
        // First we try to see if we have an in-memory instance.
        // If not found, we try to load a serialized instance.
        // If still not found, we create an empty session
        //
        // The session is initialized, cached and serialized if needed.
        BreezeSession session = sessionMap.get( uid ) ;
        
        if( session == null ) {
            log.info( "  No live session found. Checking serialized session" ) ;
            session = deserializeSession( uid ) ;
            if( session == null ) {
                log.info( "  No serialized session found." ) ;
            }
        }
        
        if( session == null ) {
            log.debug( "  Creating an empty session." ) ;
            session = new BreezeSession() ;
            session.userId = uid ;
            session.cred = cred ;
        }
        
        sessionMap.put( uid, session ) ;
        
        if( session.initializationRequired() ) {
            try {
                log.info( "  Creating new session." ) ;
                generateNewSession( session ) ;
                
                // No need to put the session back into the map. We are
                // working on the reference of the existing instance.
                
                log.debug( "  Serializing session." ) ;
                serializeSession( session ) ;
            }
            catch( Exception e ) {
                log.error( "BreezeSession initialization failed.", e ) ;
                sessionMap.remove( uid ) ;
                return null ;
            }
        }
        
        return session ;
    }
    
    private void generateNewSession( BreezeSession session ) 
        throws Exception {
        
        Map<String, String> loginFormKVPS      = null ;
        Map<String, String> tradeLoginFormKVPS = null ;
        
        String sessionId = null ;
        String sessionToken = null ;
        
        log.debug( "\nGenerating session" ) ;
        
        loginFormKVPS      = getLoginPageNameValueMap( session.cred ) ;
        tradeLoginFormKVPS = getTradeLoginIdValueMap( loginFormKVPS ) ;
        sessionId          = getSessionId( session.cred, tradeLoginFormKVPS ) ;
        sessionToken       = getSessionToken( session.cred, sessionId ) ;
        
        log.debug( "    API session id = " + sessionId ) ;
        log.debug( "    Session Token  = " + sessionToken ) ;

        session.setSessionId( sessionId ) ;
        session.setSessionToken( sessionToken ) ;
        session.setCreationTime( new Date() ) ;
        session.setValid( true ) ;
    }
    
    private Map<String, String> getLoginPageNameValueMap( BreezeCred cred ) 
            throws Exception {
        
        log.debug( "  Getting the login page" ) ;
        
        String url = API_USER_BASEURL + 
                     "/login?api_key=" + 
                     URLEncoder.encode( cred.getAppKey(), "UTF-8" ) ;
        
        Request  request        = null ;
        Response response       = null ;
        String   resBodyContent = null ;
        
        Map<String, String> inputNameValues = null ;
        
        try {
            Request.Builder builder = getRequestBuilder( url ) ;
            request = builder.get().build() ;
            
            response = client.newCall( request ).execute() ;
            log.debug( "    Server response - " + response.code() ) ;
            if( response.code() != 200 ) {
                throw new Exception( "Server error getting login page. " + 
                                     "Msg = " + resBodyContent ) ;
            }
            
            resBodyContent = response.body().string() ;
            
            inputNameValues = getInputAttrValuePairs( resBodyContent, "name" ) ;
            if( inputNameValues.isEmpty() ) {
                throw new Exception( "Invalid breeze login page obtained. " + 
                                     "Response = " + resBodyContent ) ;
            }
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return inputNameValues ;
    }
    
    private Map<String, String> getTradeLoginIdValueMap( Map<String, String> nvps ) 
        throws Exception {
        
        log.debug( "  Getting the trade login form" ) ;

        String url = API_USER_BASEURL + "/tradelogin"  ;
        
        Request     request        = null ;
        Response    response       = null ;
        String      reqBodyContent = null ;
        RequestBody requestBody    = null ;
        String      resBodyContent = null ;
        
        Map<String, String> idValMap = null ;
        
        try {
            Request.Builder builder = getRequestBuilder( url ) ;
            
            reqBodyContent = buildFormBody( nvps ) ;
            requestBody = RequestBody.create( FORM_DATA, reqBodyContent ) ;
            request = builder.post( requestBody ).build() ;
            
            response = client.newCall( request ).execute() ;
            resBodyContent = response.body().string() ;
            log.debug( "    Server response - " + response.code() ) ;
            if( response.code() != 200 ) {
                throw new Exception( "Server error providing trade login page. " + 
                                     "Msg = " + resBodyContent ) ;
            }
        
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
            if( idValMap.isEmpty() ) {
                throw new Exception( "Invalid login response obtained. " + 
                                     "Response = " + resBodyContent ) ;
            }
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return idValMap ;
    }
    
    private String getSessionId( BreezeCred cred, 
                                 Map<String, String> formFields )
        throws Exception {
        
        log.debug( "  Validating the user" ) ;

        String url = API_USER_BASEURL + "/tradelogin/validateuser"  ;
        
        String      sessionKey     = null ;
        Request     request        = null ;
        Response    response       = null ;
        String      reqBodyContent = null ;
        RequestBody requestBody    = null ;
        String      resBodyContent = null ;
        
        RSACipher rsaCipher = null ;
        Map<String, String> idValMap = null ;
        
        try {
            rsaCipher = createCipher( formFields.get( "hidenc" ) ) ;
            
            byte[] dobBytes = cred.getDob().getBytes( "UTF-8" ) ;
            byte[] pwdBytes = cred.getPassword().getBytes( "UTF-8" ) ;
            
            byte[] encDob = rsaCipher.encrypt( dobBytes ) ;
            byte[] encPwd = rsaCipher.encrypt( pwdBytes ) ;
            
            String hexEncDob = Hex.encodeHexString( encDob ) ;
            String hexEncPwd = Hex.encodeHexString( encPwd ) ;
            
            formFields.put( "txtuid",  cred.getUserId() ) ;
            formFields.put( "txtPass", "************" ) ;
            formFields.put( "txtdob",  "************" ) ;
            formFields.put( "hiddob",  hexEncDob ) ;
            formFields.put( "hidp",    hexEncPwd ) ;
            
            Request.Builder builder = getRequestBuilder( url ) ;
            
            reqBodyContent = buildFormBody( formFields ) ;
            
            requestBody = RequestBody.create( FORM_DATA, reqBodyContent ) ;
            request = builder.post( requestBody ).build() ;
            
            response = client.newCall( request ).execute() ;
            resBodyContent = response.body().string() ;
            log.debug( "    Server response - " + response.code() ) ;
            if( response.code() != 200 ) {
                throw new Exception( "Server error validating the user. " + 
                                     "Msg = " + resBodyContent ) ;
            }
            
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
            
            if( idValMap.isEmpty() || !idValMap.containsKey( "API_Session" ) ) {
                throw new Exception( "API_Session could not be obtained. " + 
                                     "Response = " + resBodyContent ) ;
            }
            sessionKey = idValMap.get( "API_Session" ) ;
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return sessionKey ;
    }
    
    // Redirect URL : http://127.0.0.1:8080/BreezeAuthCallback?userName=<userName>
    private String getSessionToken( BreezeCred cred, String apiSessionId ) 
        throws Exception {

        log.debug( "  Generating session token" ) ;

        String url = Breeze.BRZ_API_BASEURL + "/customerdetails" ;
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        
        Map<String, String> params = new HashMap<>() ;
        params.put( "SessionToken", apiSessionId ) ;
        params.put( "AppKey", cred.getAppKey() ) ;
        
        String resBody = netClient.get( url, params, null ) ;
        
        ObjectMapper mapper = new ObjectMapper() ;
        JsonNode root = mapper.readTree( resBody ) ;
        JsonNode successNode = root.get( "Success" ) ;
        
        String sessionToken = null ;
        if( successNode != null ) {
            JsonNode tokenNode = successNode.get( "session_token" ) ;
            
            if( tokenNode == null ) {
                throw new Exception( "Session token could not be obtained." ) ;
            }
            sessionToken = tokenNode.asText() ;
        }
        
        log.debug( "    BreezeSession Token = " + sessionToken ) ;        
        
        return sessionToken ;
    }
    
    private RSACipher createCipher( String hidencStr ) throws Exception {
        
        String[] parts = hidencStr.split( "~" ) ;
        String exp     = parts[0] ;
        String modulus = parts[1] ;
        
        return new RSACipher( modulus, exp, 16 ) ;
    }
    
    private String buildFormBody( Map<String, String> kvps ) 
        throws Exception {
        
        StringBuilder formBody = new StringBuilder() ;
        for( String key : kvps.keySet() ) {
            String envVal = URLEncoder.encode( kvps.get( key ), "UTF-8" ) ;
            formBody.append( key ).append( "=" ).append( envVal ) ;
            formBody.append( "&" ) ;
        }
        formBody.deleteCharAt( formBody.length()-1 ) ;
        return formBody.toString() ;
    }

    private Request.Builder getRequestBuilder( String url ) {
        
        Request.Builder builder = null ;
        builder = new Request.Builder().url( url ) ;
        for( String[] fakeHdr : FAKE_HDRS ) {
            builder.addHeader( fakeHdr[0], fakeHdr[1] ) ;
        }
        return builder ;
    }
    
    private Map<String, String> getInputAttrValuePairs( String content, String key ) {
        
        Map<String, String> kvps = new HashMap<>() ;
        Document doc = Jsoup.parse( content ) ;
        Elements inputElements = doc.select( "input" ) ;
        for( Element input : inputElements ) {
            
            String name = input.attr( key ) ;
            String value = input.attr( "value" ) ;
            
            kvps.put( name, value ) ;
        }
        return kvps ;
    }

    private BreezeSession deserializeSession( String userId ) {
        
        File serFile = getSessionSerFile( userId ) ;
        FileInputStream fIs = null ;
        ObjectInputStream oIs = null ;
        BreezeSession session = null ;
        
        try {
            if( serFile.exists() ) {
                log.debug( "  Loading persistent session id state" ) ;
                fIs = new FileInputStream( serFile ) ;
                oIs = new ObjectInputStream( fIs ) ;
                
                session = ( BreezeSession )oIs.readObject() ; 
                log.debug( "    Read state" ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error serializing session.", e ) ;
        }
        finally {
            if( oIs != null ) {
                try {
                    oIs.close() ;
                }
                catch( Exception e ) {
                    log.error( "Error closing streams.", e ) ;
                }
            }
        }
        
        return session ;
    }
    
    private void serializeSession( BreezeSession session ) {
        
        File serFile = getSessionSerFile( session.userId ) ;
        ObjectOutputStream oOs = null ;
        FileOutputStream fOs = null ;
        
        try {
            if( session != null ) {
                log.debug( "  Persisting session id" ) ;
                fOs = new FileOutputStream( serFile ) ;
                oOs = new ObjectOutputStream( fOs ) ;
                
                oOs.writeObject( session ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error deserializing session.", e ) ;
        }
        finally {
            if( oOs != null ) {
                try {
                    oOs.flush() ;
                    oOs.close() ;
                }
                catch( IOException e ) {
                    log.error( "Error closing streams.", e ) ;
                }
            }
        }    
    }
    
    private File getSessionSerFile( String userId ) {
        
        String fileName = "session-" + userId.toLowerCase() + ".ser" ;
        File   serDir   = Breeze.instance().getSerializationDir() ;
        File   serFile  = new File( serDir, fileName ) ;

        log.debug( "    Ser file - " + serFile.getAbsolutePath() ) ;
        return serFile ;
    }
}
