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
import com.sandy.capitalyst.server.core.util.RSACipher ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

import lombok.Data ;
import okhttp3.MediaType ;
import okhttp3.OkHttpClient ;
import okhttp3.Request ;
import okhttp3.RequestBody ;
import okhttp3.Response ;

public class BreezeSession {

    private static final Logger log = Logger.getLogger( BreezeSession.class ) ;
    
    private static final String API_USER_BASEURL = "https://api.icicidirect.com/apiuser" ;
    public  static final String BRZ_API_BASEURL  = "https://api.icicidirect.com/breezeapi/api/v1" ;
    
    private static final MediaType FORM_DATA = MediaType.parse( "application/x-www-form-urlencoded" ) ;
    
    @Data
    public static class Session implements Serializable {
        private static final long serialVersionUID = -1427026265072334302L ;
        private String sessionId = null ;
        private String sessionToken = null ;
        private Date creationTime = null ;
    }
    
    private static final String FAKE_HDRS[][] = {
        { "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36" }
    } ;
    
    private static BreezeSession instance = null ;
    
    private OkHttpClient client = null ;
    
    private String appKey      = null ;
    private String userId      = null ;
    private String password    = null ;
    private String dob         = null ;
    private String secretKey   = null ;
    private File   persistDir  = null ;
    private File   persistFile = null ;
    
    private Session session = null ;
    
    public static BreezeSession instance() throws Exception {
        if( instance == null ) {
            instance = new BreezeSession() ;
        }
        
        if( instance.appKey != null ) { 
            // Implies that the instance has been initialized
            if( instance.sessionGenerationRequired() ) {
                instance.generateSessionToken() ;
            }
        }
        return instance ;
    }
    
    private BreezeSession() {}
    
    public void initialize( String appKey, String userId, 
                            String password, String dob,
                            String secretKey,
                            File persistenceDir ) 
        throws Exception {
        
        log.debug( "\nInitializing BreezeSession" ) ;
        
        this.appKey     = appKey ;
        this.userId     = userId ;
        this.password   = password ;
        this.secretKey  = secretKey ;
        this.dob        = dob ;
        this.persistDir = persistenceDir ;
        
        this.persistFile = new File( this.persistDir, "breeze-session.key" ) ;
        
        this.client = new OkHttpClient.Builder()
                                      .connectTimeout( 60, TimeUnit.SECONDS )
                                      .readTimeout( 60, TimeUnit.SECONDS )
                                      .writeTimeout( 60, TimeUnit.SECONDS )
                                      .build() ;
        
        loadPersistedSession() ;
        
        if( sessionGenerationRequired() ) {
            generateSessionToken() ;
        }
    }
    
    public Session getSession() {
        return this.session ;
    }
    
    public String getSecretKey() {
        return this.secretKey ;
    }
    
    public String getAppKey() {
        return this.appKey ;
    }
    
    private void generateSessionToken() throws Exception {
        
        Map<String, String> loginFormKVPS      = null ;
        Map<String, String> tradeLoginFormKVPS = null ;
        
        String apiSessionId = null ;
        String sessionToken = null ;
        
        log.debug( "\nGenerating BreezeSession session id" ) ;
        
        loginFormKVPS      = getLoginPageNameValueMap() ;
        tradeLoginFormKVPS = getTradeLoginIdValueMap( loginFormKVPS ) ;
        apiSessionId       = getSessionId( tradeLoginFormKVPS ) ;
        sessionToken       = getSessionToken( apiSessionId ) ;
        
        log.debug( "  API session id = " + apiSessionId ) ;
        log.debug( "  Session Token  = " + sessionToken ) ;
        
        this.session = new Session() ;
        this.session.setSessionId( apiSessionId ) ;
        this.session.setSessionToken( sessionToken ) ;
        this.session.setCreationTime( new Date() ) ;
        
        persistSession() ;
    }
    
    private Map<String, String> getLoginPageNameValueMap() throws Exception {
        
        log.debug( "  Getting the login page" ) ;
        
        String url = API_USER_BASEURL + "/login?api_key=" + this.appKey  ;
        
        Request  request        = null ;
        Response response       = null ;
        String   resBodyContent = null ;
        
        Map<String, String> inputNameValues = null ;
        
        try {
            Request.Builder builder = getRequestBuilder( url ) ;
            request = builder.get().build() ;
            
            response = client.newCall( request ).execute() ;
            log.debug( "    Server response - " + response.code() ) ;
            
            resBodyContent = response.body().string() ;
            
            inputNameValues = getInputAttrValuePairs( resBodyContent, "name" ) ;
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
        
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return idValMap ;
    }
    
    private String getSessionId( Map<String, String> formFields )
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
            
            byte[] dobBytes = this.dob.getBytes( "UTF-8" ) ;
            byte[] pwdBytes = this.password.getBytes( "UTF-8" ) ;
            
            byte[] encDob = rsaCipher.encrypt( dobBytes ) ;
            byte[] encPwd = rsaCipher.encrypt( pwdBytes ) ;
            
            String hexEncDob = Hex.encodeHexString( encDob ) ;
            String hexEncPwd = Hex.encodeHexString( encPwd ) ;
            
            formFields.put( "txtuid",  this.userId ) ;
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
            
            idValMap = getInputAttrValuePairs( resBodyContent, "id" ) ;
            sessionKey = idValMap.get( "API_Session" ) ;
        }
        finally {
            if( response != null ) {
                response.body().close() ;
            }
        }
        
        return sessionKey ;
    }
    
    private String getSessionToken( String apiSessionId ) 
        throws Exception {

        log.debug( "  Generating session token" ) ;

        String url = BRZ_API_BASEURL + "/customerdetails" ;
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "SessionToken", apiSessionId ) ;
        params.put( "AppKey", this.appKey ) ;
        
        String resBody = netClient.get( url, params ) ;
        
        ObjectMapper mapper = new ObjectMapper() ;
        JsonNode root = mapper.readTree( resBody ) ;
        JsonNode successNode = root.get( "Success" ) ;
        
        String sessionToken = null ;
        if( successNode != null ) {
            JsonNode tokenNode = successNode.get( "session_token" ) ;
            sessionToken = tokenNode.asText() ;
        }
        
        log.debug( "    Session Token = " + sessionToken ) ;        
        
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

    private void loadPersistedSession() {
        
        FileInputStream fIs = null ;
        ObjectInputStream oIs = null ;
        
        try {
            if( this.persistFile.exists() ) {
                log.debug( "  Loading persistent session id state" ) ;
                fIs = new FileInputStream( this.persistFile ) ;
                oIs = new ObjectInputStream( fIs ) ;
                
                session = ( Session )oIs.readObject() ; 
            }
        }
        catch( Exception e ) {
            log.error( "Error saving session key.", e ) ;
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
    }
    
    private void persistSession() {
        
        ObjectOutputStream oOs = null ;
        FileOutputStream fOs = null ;
        
        try {
            if( this.session != null ) {
                log.debug( "  Persisting session id" ) ;
                fOs = new FileOutputStream( persistFile ) ;
                oOs = new ObjectOutputStream( fOs ) ;
                
                oOs.writeObject( session ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error loading session key.", e ) ;
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

    private boolean sessionGenerationRequired() {
        if( this.session == null ) {
            log.debug( "  New session id required." ) ;
            return true ;
        }
        else if( StringUtil.isEmptyOrNull( this.session.sessionToken ) ) {
            return true ;
        } 
        else {
            Date todayStart = new Date() ;
            todayStart = DateUtils.truncate( todayStart, Calendar.DAY_OF_MONTH ) ;
            if( session.creationTime.before( todayStart ) ) {
                log.debug( "  New session id required. Old one expired." ) ;
                return true ;
            }
        }
        return false ;
    }
}
