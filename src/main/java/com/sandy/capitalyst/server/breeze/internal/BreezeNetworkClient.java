package com.sandy.capitalyst.server.breeze.internal;

import static org.apache.http.entity.ContentType.APPLICATION_JSON ;

import java.net.URI ;
import java.security.MessageDigest ;
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

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;

import lombok.Getter ;

public class BreezeNetworkClient {

    private static final Logger log = Logger.getLogger( BreezeNetworkClient.class ) ;
    
    public static class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "GET";
        public String getMethod() {
            return METHOD_NAME;
        }
    }
    
    public static class BreezeAPIException extends Exception {
        
        private static final long serialVersionUID = 1L ;
        
        @Getter private int status = 0 ;
        @Getter private String errorMsg = null ;
        
        BreezeAPIException( int status, String msg ) {
            this.status = status ;
            this.errorMsg = msg ;
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
    private BreezeNetworkRateLimiter rateLimiter = null ;
    
    private boolean netLogEnabled = false ;

    private BreezeNetworkClient() {
        
        httpClient  = HttpClientBuilder.create().build() ;
        objMapper   = new ObjectMapper() ;
        rateLimiter = new BreezeNetworkRateLimiter() ;
        
        SDF.setTimeZone( TimeZone.getTimeZone( "GMT" ) ) ;
        
        standardHeaders.put( "User-Agent",      "Custom HTTP Client" ) ;
        standardHeaders.put( "Accept",          "*/*" ) ;
        standardHeaders.put( "Cache-Control",   "no-cache" ) ;
        standardHeaders.put( "Host",            "api.icicidirect.com" ) ;
        standardHeaders.put( "Accept-Encoding", "gzip, deflate, br" ) ;
        standardHeaders.put( "Connection",      "keep-alive" ) ;
        
    }
    
    public String get( String urlStr, BreezeSession session ) 
            throws Exception {
        
        return get( urlStr, null, null, session ) ;
    }
        
    public String get( String urlStr, Map<String, String> bodyMap, 
                       BreezeSession session ) 
            throws Exception {
        
        String body = objMapper.writeValueAsString( bodyMap ) ;
        return get( urlStr, null, body, session ) ;
    }
    
    private String get( String urlStr, Map<String, String> customHdrs, 
                        String body, BreezeSession session )
        throws Exception {
        
        netLogEnabled = Breeze.instance()
                              .getNVPCfg()
                              .isNetworkLoggingEnabled() ;
        
        if( netLogEnabled ) {
            if( session == null ) {
                log.info( "Making a call with null session" ) ;
            }
            log.debug( "GET " + urlStr ) ;
        }
        
        HttpGetWithEntity request = new HttpGetWithEntity() ;
        HttpResponse      response = null ;
        HttpEntity        responseEntity = null ;
        
        if( netLogEnabled ) {
            log.debug( "  Body:" ) ;
            log.debug( body ) ;
        }
        
        String responseStr ;
        try {
            request.setURI( new URI( urlStr ) ) ;
            request.setEntity( new StringEntity( body, APPLICATION_JSON ) ) ;
            
            setHeaders( request, customHdrs, body, session ) ;
            
            rateLimiter.throttle( session ) ;
            
            response = httpClient.execute( request ) ;
            
            responseStr = null ;
            responseEntity = response.getEntity() ;
            
            if( netLogEnabled ) {
                log.debug( "  Response:" ) ;
                log.debug( "    Status = " + response.getStatusLine().getStatusCode() );
            }
            
            if( response.getStatusLine().getStatusCode() != 200 ) {
                throw new BreezeAPIException( response.getStatusLine().getStatusCode(),
                                              response.getEntity().toString() ) ;
            }
            
            if( responseEntity != null ) {
                int    len     = (int)responseEntity.getContentLength() ;
                byte[] content = new byte[len] ;
                IOUtils.readFully( responseEntity.getContent(), content );
                
                responseStr = new String( content ) ;
                if( netLogEnabled ) {
                    log.debug( "  Response body:" ) ; 
                    log.debug( "    " + responseStr ) ;
                }
            }
        }
        finally {
            request.releaseConnection() ;
        }
        
        return responseStr ;
    }
    
    private void setHeaders( HttpRequestBase req, 
                             Map<String, String> customHeaders,
                             String body, BreezeSession session ) 
        throws Exception {
        
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
                                     BreezeSession session ) 
        throws Exception {
        
        String timestamp = SDF.format( new Date() ) ;
        String checksum  = generateChecksum( timestamp, body, 
                                             session.getCred().getSecretKey() ) ;
        
        if( netLogEnabled ) {
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
                                     String secretKey ) 
        throws Exception {
        
        String rawChecksum = timestamp + body + secretKey ;

        MessageDigest sha256 = MessageDigest.getInstance( "SHA-256" ) ;
        sha256.reset();

        byte[] digested  = sha256.digest( rawChecksum.getBytes() ) ;
        
        return Hex.encodeHexString( digested ) ;
    }
}
