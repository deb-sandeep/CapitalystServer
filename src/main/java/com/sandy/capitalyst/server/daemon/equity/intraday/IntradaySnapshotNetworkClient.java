package com.sandy.capitalyst.server.daemon.equity.intraday;

import java.io.BufferedReader ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.nio.charset.StandardCharsets ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.http.HttpResponse ;
import org.apache.http.client.CookieStore ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.config.CookieSpecs ;
import org.apache.http.client.config.RequestConfig ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpRequestBase ;
import org.apache.http.cookie.Cookie ;
import org.apache.http.impl.client.BasicCookieStore ;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.http.util.EntityUtils ;
import org.apache.log4j.Logger ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.core.util.StringUtil ;

@Component
public class IntradaySnapshotNetworkClient {

    private static final Logger log = Logger.getLogger( IntradaySnapshotNetworkClient.class ) ;
    
    private HttpClient    httpClient  = null ;
    private RequestConfig reqConfig   = null ;
    private CookieStore   cookieStore = null ;
    
    private boolean netLogEnabled = true ;

    public IntradaySnapshotNetworkClient() {
        
        cookieStore = new BasicCookieStore() ;
        
        reqConfig = RequestConfig.custom()
                                 .setCookieSpec(CookieSpecs.STANDARD)
                                 .build() ;
        
        httpClient  = HttpClientBuilder.create()
                                       .setDefaultRequestConfig( reqConfig )
                                       .setDefaultCookieStore( cookieStore )
                                       .build() ;
    }
    
    public String get( String url, String hdrFile ) 
            throws Exception {
            
        HttpGet      request        = null ;
        HttpResponse response       = null ;
        int          responseCode   = 0 ;
        String       resBodyContent = null ;
        
        request = new HttpGet( url ) ;
        setHeaders( request, hdrFile ) ;
        
        response       = httpClient.execute( request ) ;
        responseCode   = response.getStatusLine().getStatusCode() ;
        
        if( netLogEnabled ) {
            log.debug( "  Response:" ) ;
            log.debug( "    Status = " + responseCode );
        }
        
        resBodyContent = EntityUtils.toString(response.getEntity(), 
                                              StandardCharsets.UTF_8);

        return resBodyContent ;
    }
    
    private void setHeaders( HttpRequestBase req, String hdrFile ) 
        throws Exception {

        Map<String, String> customHdrs = loadHeaders( hdrFile ) ;
        
        for( String header : customHdrs.keySet() ) {
            String val = customHdrs.get( header ) ;
            req.addHeader( header, val ) ;
            if( netLogEnabled ) {
                log.debug( "      " + header + " = " + val ) ;
            }
        }
    }

    private Map<String, String> loadHeaders( String headerResourceName ) 
        throws Exception {
        
        InputStream is = null ;
        BufferedReader reader = null ;
        Map<String, String> headers = new HashMap<>() ;
        
        if( StringUtil.isEmptyOrNull( headerResourceName ) ) {
            return headers ;
        }
        
        String resPath = "/http_headers/" + headerResourceName ;
        
        is = getClass().getResourceAsStream( resPath ) ;
        if( is == null ) {
            throw new RuntimeException( "Header file not found @ " + resPath ) ;
        }
        
        reader = new BufferedReader( new InputStreamReader( is ) ) ;
        String line = reader.readLine() ;
        while( line != null ) {
            if( StringUtil.isNotEmptyOrNull( line ) ) {
                if( !line.trim().startsWith( "#" ) ) {
                    String[] parts = line.trim().split( ": " ) ;
                    headers.put( parts[0].trim(), parts[1].trim() ) ;
                }
            }
            line = reader.readLine() ;
        }
        
        return headers ;
    }

    public boolean hasCookie( String name ) {
        
        List<Cookie> cookies = null ;
        cookies = cookieStore.getCookies() ;
        
        if( cookies != null && !cookies.isEmpty() ) {
            for( Cookie cookie : cookies ) {
                if( cookie.getName().equals( name ) && 
                    !cookie.isExpired( new Date() ) ) {
                    return true ;
                }
            }
        }
        return false ;
    }
    
    public String getCookieValue( String name ) {
        
        List<Cookie> cookies = null ;
        cookies = cookieStore.getCookies() ;
        
        if( cookies != null && !cookies.isEmpty() ) {
            for( Cookie cookie : cookies ) {
                if( cookie.getName().equals( name ) ) {
                    return cookie.getValue() ;
                }
            }
        }
        return null ;
    }
    
}
