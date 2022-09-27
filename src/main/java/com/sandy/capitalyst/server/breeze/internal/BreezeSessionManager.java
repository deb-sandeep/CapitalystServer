package com.sandy.capitalyst.server.breeze.internal;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;
import java.io.UnsupportedEncodingException ;
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
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.core.util.RSACipher ;
import com.sandy.capitalyst.server.core.util.RSACipher.RSACipherException ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

import lombok.Getter ;
import okhttp3.HttpUrl ;
import okhttp3.MediaType ;
import okhttp3.OkHttpClient ;
import okhttp3.Request ;
import okhttp3.RequestBody ;
import okhttp3.Response ;

public class BreezeSessionManager {

    private static final Logger log = Logger.getLogger( BreezeSessionManager.class ) ;
    
    private static final String API_USER_BASEURL = "https://api.icicidirect.com/apiuser" ;
    
    private static final MediaType FORM_DATA = MediaType.parse( "application/x-www-form-urlencoded" ) ;
    
    public static class BreezeSession implements Serializable {
        
        private static final long serialVersionUID = -1427026265072334302L ;

        @Getter private String  userId          = null ;
        @Getter private String  sessionId       = null ;
        @Getter private String  sessionToken    = null ;
        @Getter private Date    creationTime    = null ;
        @Getter private boolean dayLimitReached = false ;
        
        @Getter private BreezeCred cred = null ;
        
        private BreezeSession( BreezeCred cred ) {
            this.cred = cred ;
        }
        
        private boolean initializationRequired() {
            
            if( StringUtil.isEmptyOrNull( this.sessionToken ) ) {
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
    
    private boolean netLogEnabled = false ;
    
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
    
    public void invalidateAllSessions() {
        sessionMap.values().forEach( session -> {
            deletePersistedSession( session ) ;
        } ) ;
        sessionMap.clear() ;
    }
    
    public void invalidateSession( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.remove( cred.getUserId() ) ;
        if( session != null ) {
            deletePersistedSession( session ) ;
        }
    }
    
    public void setDayLimitReached( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.remove( cred.getUserId() ) ;
        if( session != null ) {
            session.dayLimitReached = true ;
            serializeSession( session ) ;
        }
    }
    
    public boolean isWithinDayRateLimit( BreezeCred cred ) {
        
        BreezeSession session = sessionMap.remove( cred.getUserId() ) ;
        if( session != null ) {
            if( session.isDayLimitReached() ) {
                return false ;
            }
        }
        return true ;
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
        
        if( session == null || session.initializationRequired() ) {
            try {
                log.info( "  Creating new session." ) ;
                session = createNewSession( cred ) ;

                sessionMap.put( uid, session ) ;
                
                log.debug( "  Serializing session." ) ;
                serializeSession( session ) ;
            }
            catch( BreezeException e ) {
                log.error( "BreezeSession initialization failed.", e ) ;
                sessionMap.remove( uid ) ;
                return null ;
            }
        }
        
        return session ;
    }
    
    private BreezeSession createNewSession( BreezeCred cred ) 
        throws BreezeException {
        
        Map<String, String> loginFormKVPS      = null ;
        Map<String, String> tradeLoginFormKVPS = null ;
        
        String sessionId = null ;
        String sessionToken = null ;
        
        BreezeSession session = null ;
        
        log.debug( "\n  Creating a new session" ) ;
        
        netLogEnabled = Breeze.instance()
                              .getNVPCfg()
                              .isNetworkLoggingEnabled() ;
        
        loginFormKVPS      = getLoginPageNameValueMap( cred ) ;
        tradeLoginFormKVPS = getTradeLoginIdValueMap( cred, loginFormKVPS ) ;
        sessionId          = getSessionId( cred, tradeLoginFormKVPS ) ;
        sessionToken       = getSessionToken( cred, sessionId ) ;
        
        log.debug( "    API session id = " + sessionId ) ;
        log.debug( "    Session Token  = " + sessionToken ) ;

        session = new BreezeSession( cred ) ;
        
        session.userId       = cred.getUserId() ;
        session.sessionId    = sessionId ;
        session.sessionToken = sessionToken ;
        session.creationTime = new Date() ;
        
        return session ;
    }
    
    private Map<String, String> getLoginPageNameValueMap( BreezeCred cred ) 
            throws BreezeException {
        
        log.debug( "  Getting the login page" ) ;
        
        String   url            = null ;
        Request  request        = null ;
        Response response       = null ;
        String   resBodyContent = null ;
        
        Map<String, String> inputNameValues = null ;
        
        try {
            url = API_USER_BASEURL + "/login?api_key=" + 
                  URLEncoder.encode( cred.getAppKey(), "UTF-8" ) ;
            
            Request.Builder builder = getRequestBuilder( url ) ;
            request = builder.get().build() ;
            
            resBodyContent = executeOkHttpRequest( request, cred, "" ) ;
            
            inputNameValues = getInputAttrValuePairs( resBodyContent, "name" ) ;
            
            if( inputNameValues.isEmpty() ) {
                throw BreezeException.sessionError( cred.getUserName(),
                                                    "Login page",
                                                    resBodyContent ) ;
            }
        }
        catch( UnsupportedEncodingException e ) {
            // Ignore. This will never happen with a hard coded UTF-8 value
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return inputNameValues ;
    }
    
    private Map<String, String> getTradeLoginIdValueMap( 
                                     BreezeCred cred, Map<String, String> nvps ) 
        throws BreezeException {
        
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
            requestBody    = RequestBody.create( FORM_DATA, reqBodyContent ) ;
            request        = builder.post( requestBody ).build() ;
            
            resBodyContent = executeOkHttpRequest( request, cred,
                                                   reqBodyContent ) ;
            
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
            
            if( idValMap.isEmpty() ) {
                throw BreezeException.sessionError( cred.getUserName(),
                                                    "Pre login", 
                                                    resBodyContent ) ;
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
        throws BreezeException {
        
        log.debug( "  Validating the user and getting session id" ) ;

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
            requestBody    = RequestBody.create( FORM_DATA, reqBodyContent ) ;
            request        = builder.post( requestBody ).build() ;
            
            resBodyContent = executeOkHttpRequest( request, cred,
                                                   reqBodyContent ) ;
            
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
            
            if( idValMap.isEmpty() || 
                !idValMap.containsKey( "API_Session" ) ) {
                
                throw BreezeException.sessionError( cred.getUserName(),
                                                    "API_Session generation", 
                                                    resBodyContent ) ;
            }
            
            sessionKey = idValMap.get( "API_Session" ) ;
        }
        catch( RSACipherException e ) {
            throw BreezeException.appException( e ) ;
        }
        catch( UnsupportedEncodingException e ) {
            // This will never happen because of hard coded UTF-8 value
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return sessionKey ;
    }
    
    private String executeOkHttpRequest( Request request, 
                                         BreezeCred cred,
                                         String requestBody ) 
        throws BreezeException {
        
        final String I0 = "    " ;
        final String I1 = "      " ;
        
        Response response       = null ;
        String   resBodyContent = null ;
        
        BreezeNetworkRateLimiter.instance().throttle( cred ) ;
        
        if( netLogEnabled ) {
            log.debug( I0 + "Executing " + request.method() + " request." ) ;
            log.debug( I0 + "URL : " + request.url().url().toString() ) ;
            
            log.debug( I0 + "Parameters :" ) ;
            HttpUrl url = request.url() ;
            url.queryParameterNames().forEach( param -> {
                log.debug( I1 + param + " - " + url.queryParameter( param ) ) ;
            });
            
            log.debug( I0 + "Headers : " ) ;
            request.headers().names().forEach( hdrName -> {
                log.debug( I1 + hdrName + " - " + request.header( hdrName ) ) ;
            }) ;
            
            if( StringUtil.isNotEmptyOrNull( requestBody ) ) {
                log.debug( I0 + "Body : " + requestBody ) ;
            }
        }
        
        try {
            response = client.newCall( request ).execute() ;
            resBodyContent = response.body().string() ;
            
            if( netLogEnabled ) {
                log.debug( I0 + "Response code : " + response.code() ) ; 
                log.debug( I0 + "Response body : " + resBodyContent ) ;
            }
        }
        catch( IOException e ) {
            log.error( "Exception invoking HTTP request.", e ) ;
            throw BreezeException.appException( e ) ;
        }
        
        if( response.code() != 200 ) {
            throw BreezeException.httpError( response.code(), resBodyContent ) ;
        }
        else {
            if( resBodyContent.startsWith( "Limit exceed:" ) ) {
                if( resBodyContent.contains( "day" ) ) {
                    throw BreezeException.dayRateExceed( cred.getUserName() ) ;
                }
                else {
                    throw BreezeException.minRateExceed( cred.getUserName() ) ;
                }
            }
        }
        
        return resBodyContent ;
    }
    
    // Redirect URL : http://127.0.0.1:8080/BreezeAuthCallback?userName=<userName>
    private String getSessionToken( BreezeCred cred, String apiSessionId ) 
        throws BreezeException {

        log.debug( "  Generating session token" ) ;

        String sessionToken ;
        try {
            sessionToken = null ;
            
            String url = Breeze.BRZ_API_BASEURL + "/customerdetails" ;
            BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
            
            Map<String, String> params = new HashMap<>() ;
            params.put( "SessionToken", apiSessionId ) ;
            params.put( "AppKey", cred.getAppKey() ) ;
            
            String resBody = netClient.get( url, params, null ) ;
            
            ObjectMapper mapper = new ObjectMapper() ;
            JsonNode root = mapper.readTree( resBody ) ;
            JsonNode successNode = root.get( "Success" ) ;
            
            if( successNode != null ) {
                JsonNode tokenNode = successNode.get( "session_token" ) ;
                
                if( tokenNode == null ) {
                    throw BreezeException.sessionError( cred.getUserName(), 
                                                        "Getting session token", 
                                                        resBody ) ;
                }
                sessionToken = tokenNode.asText() ;
            }
            
            log.debug( "    BreezeSession Token = " + sessionToken ) ;
        }
        catch( IOException e ) {
            throw BreezeException.appException( e ) ;
        }
        
        return sessionToken ;
    }
    
    private RSACipher createCipher( String hidencStr ) 
            throws RSACipherException {
        
        String[] parts = hidencStr.split( "~" ) ;
        String exp     = parts[0] ;
        String modulus = parts[1] ;
        
        return new RSACipher( modulus, exp, 16 ) ;
    }
    
    private String buildFormBody( Map<String, String> kvps ) {
        
        StringBuilder formBody = new StringBuilder() ;
        try {
            for( String key : kvps.keySet() ) {
                String envVal = URLEncoder.encode( kvps.get( key ), "UTF-8" ) ;
                formBody.append( key ).append( "=" ).append( envVal ) ;
                formBody.append( "&" ) ;
            }
            formBody.deleteCharAt( formBody.length()-1 ) ;
        }
        catch( UnsupportedEncodingException e ) {
            // This will never happen with a hard coded UTF-8 value
        }
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
    
    private void deletePersistedSession( BreezeSession session ) {
        
        File serFile = getSessionSerFile( session.userId ) ;
        if( serFile.exists() ) {
            serFile.delete() ;
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
