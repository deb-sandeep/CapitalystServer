package com.sandy.capitalyst.server.breeze.internal;

import static org.apache.http.entity.ContentType.APPLICATION_JSON ;

import java.io.IOException ;
import java.net.URI ;
import java.net.URISyntaxException ;
import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.TimeZone ;

import org.apache.commons.codec.binary.Hex ;
import org.apache.commons.io.IOUtils ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase ;
import org.apache.http.client.methods.HttpRequestBase ;
import org.apache.http.entity.StringEntity ;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.log4j.Logger ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;
import com.univocity.parsers.common.input.EOFException ;

public class BreezeNetworkClient {

    private static final Logger log = Logger.getLogger( BreezeNetworkClient.class ) ;
    
    public static class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "GET";
        public String getMethod() {
            return METHOD_NAME;
        }
    }
    
    private static BreezeNetworkClient instance = null ;
    private static SimpleDateFormat SDF = new SimpleDateFormat( Breeze.ISO8601_FMT ) ;
    
    static {
        SDF.setTimeZone( TimeZone.getTimeZone( "GMT" ) ) ;
    }
    
    public static BreezeNetworkClient instance() {
        if( instance == null ) {
            instance = new BreezeNetworkClient() ;
        }
        return instance ;
    }
    
    private HttpClient httpClient = null ;
    private Map<String, String> standardHeaders = new HashMap<>() ;
    private ObjectMapper objMapper = null ;
    
    private boolean netLogEnabled = false ;
    private boolean printAPIResponse = false ;

    private BreezeNetworkClient() {
        
        httpClient  = HttpClientBuilder.create().build() ;
        objMapper   = new ObjectMapper() ;
        
        SDF.setTimeZone( TimeZone.getTimeZone( "GMT" ) ) ;
        
        standardHeaders.put( "User-Agent",      "Custom HTTP Client" ) ;
        standardHeaders.put( "Accept",          "*/*" ) ;
        standardHeaders.put( "Cache-Control",   "no-cache" ) ;
        standardHeaders.put( "Host",            "api.icicidirect.com" ) ;
        standardHeaders.put( "Accept-Encoding", "gzip, deflate, br" ) ;
        standardHeaders.put( "Connection",      "keep-alive" ) ;
    }
    
    public String get( String urlStr, 
                       Map<String, String> bodyMap,
                       BreezeCred cred ) 
        throws BreezeException {
        
        String body = null ;
        try {
            body = objMapper.writeValueAsString( bodyMap ) ;
        }
        catch( JsonProcessingException e ) {
            throw BreezeException.appException( e ) ;
        }
        
        return get( urlStr, null, body, null, cred ) ;
    }
    
    public String get( String urlStr, 
                       Map<String, String> bodyMap, 
                       BreezeSession session ) 
        throws BreezeException {
        
        String body = null ;
        
        try {
            body = objMapper.writeValueAsString( bodyMap ) ;
        }
        catch( JsonProcessingException e ) {
            throw BreezeException.appException( e ) ;
        }
        
        return get( urlStr, null, body, session, session.getCred() ) ;
    }
    
    private String get( String urlStr, 
                        Map<String, String> customHdrs, 
                        String body, 
                        BreezeSession session,
                        BreezeCred cred )
        throws BreezeException {
        
        BreezeNVPConfig cfg = Breeze.instance().getNVPCfg() ;
        
        netLogEnabled    = cfg.isNetworkLoggingEnabled() ;
        printAPIResponse = cfg.isPrintAPIResponse() ;
        
        HttpGetWithEntity request     = new HttpGetWithEntity() ;
        String            responseStr = null ;
        
        if( netLogEnabled ) {
            if( session == null ) {
                log.info( "Making a call without an active session." ) ;
            }
            log.debug( "GET " + urlStr ) ;
            log.debug( "  Body:" ) ;
            log.debug( body ) ;
        }
        
        try {
            request.setURI( new URI( urlStr ) ) ;
            request.setEntity( new StringEntity( body, APPLICATION_JSON ) ) ;
            
            setHeaders( request, customHdrs, body, session ) ;
            
            responseStr = executeHttpRequest( request, cred ) ;
        }
        catch( URISyntaxException e ) {
            throw BreezeException.appException( e ) ;
        }
        finally {
            request.releaseConnection() ;
        }
        
        return responseStr ;
    }
    
    private String executeHttpRequest( HttpGetWithEntity request, 
                                       BreezeCred cred ) 
            throws BreezeException {
            
        HttpResponse response       = null ;
        int          responseCode   = 0 ;
        String       resBodyContent = null ;
        HttpEntity   responseEntity = null ;
        
        String userName = cred.getUserName() ;
        
        try {
            BreezeNetworkRateLimiter.instance().throttle( cred ) ;
            
            response       = httpClient.execute( request ) ;
            responseCode   = response.getStatusLine().getStatusCode() ;
            responseEntity = response.getEntity() ;
            
            if( netLogEnabled ) {
                log.debug( "  Response:" ) ;
                log.debug( "    Status = " + response.getStatusLine().getStatusCode() );
            }
            
            if( responseEntity != null ) {
                
                int    len     = (int)responseEntity.getContentLength() ;
                byte[] content = new byte[len] ;
                
                IOUtils.readFully( responseEntity.getContent(), content );
                
                resBodyContent = new String( content ) ;
                
                if( netLogEnabled && printAPIResponse ) {
                    log.debug( "  Response body:" ) ; 
                    log.debug( "    " + resBodyContent ) ;
                }
            }
        }
        catch( IOException | EOFException e ) {
            throw BreezeException.appException( e ) ;
        }
        
        if( responseCode != 200 ) {
            throw BreezeException.httpError( responseCode, resBodyContent ) ;
        }
        else {
            if( resBodyContent.startsWith( "Limit exceed:" ) ) {
                if( resBodyContent.contains( "day" ) ) {
                    throw BreezeException.dayRateExceed( userName ) ;
                }
                else {
                    throw BreezeException.minRateExceed( userName ) ;
                }
            }
        }
        
        return resBodyContent ;
    }
    
    private void setHeaders( HttpRequestBase req, 
                             Map<String, String> customHeaders,
                             String body, 
                             BreezeSession session ) {
        
        if( netLogEnabled ) {
            log.debug( "  Headers:" ) ;
            log.debug( "    Standard Headers:" ) ;
        }
        
        for( String header : standardHeaders.keySet() ) {
            String val = standardHeaders.get( header ) ;
            req.addHeader( header, val ) ;
            if( netLogEnabled ) {
                log.debug( "      " + header + " = " + val ) ;
            }
        }

        if( customHeaders != null && !customHeaders.isEmpty() ) {
            if( netLogEnabled ) {
                log.debug( "    Custom Headers:" ) ;
            }
            for( String header : customHeaders.keySet() ) {
                String val = customHeaders.get( header ) ;
                req.addHeader( header, val ) ;
                if( netLogEnabled ) {
                    log.debug( "      " + header + " = " + val ) ;
                }
            }
        }
        
        if( session != null ) {
            addChecksumHeaders( req, body, session ) ;
        }
    }
    
    private void addChecksumHeaders( HttpRequestBase req, String body, 
                                     BreezeSession session ) {
        
        String timestamp = SDF.format( new Date() ) ;
        String checksum  = generateChecksum( timestamp, body, 
                                             session.getCred().getSecretKey() ) ;
        if( netLogEnabled ) {
            log.debug( "    Cred:" ) ;
            log.debug( "      User ID        = " + session.getCred().getUserId() ) ;
            log.debug( "      App Key        = " + session.getCred().getAppKey() ) ;
            log.debug( "      Secret Key     = " + session.getCred().getSecretKey() );
            log.debug( "    Session:" ) ;
            log.debug( "      Session ID     = " + session.getSessionId() ) ;
            log.debug( "      Session Token  = " + session.getSessionToken() );
            log.debug( "    Checksum Headers:" ) ;
            log.debug( "      X-Checksum     = token " + checksum ) ;
            log.debug( "      X-Timestamp    = " + timestamp ) ;
            log.debug( "      X-AppKey       = " + session.getCred().getAppKey() ) ;
            log.debug( "      X-SessionToken = " + session.getSessionToken() ) ;
        }
        
        req.addHeader( "X-Checksum",     "token " + checksum ) ;
        req.addHeader( "X-Timestamp",    timestamp ) ;
        req.addHeader( "X-AppKey",       session.getCred().getAppKey() ) ;
        req.addHeader( "X-SessionToken", session.getSessionToken() ) ;    
    }
    
    private String generateChecksum( String timestamp, String body,
                                     String secretKey ) {
        
        String response = null ;
        
        try {
            String rawChecksum = timestamp + body + secretKey ;
            MessageDigest sha256 = MessageDigest.getInstance( "SHA-256" ) ;
            
            sha256.reset();

            byte[] digested  = sha256.digest( rawChecksum.getBytes() ) ;
            response = Hex.encodeHexString( digested ) ;
        }
        catch( NoSuchAlgorithmException e ) {
            // This will never happen with a hard coded value.
        }
        
        return response ;
    }

}
