package com.sandy.capitalyst.server.core.util;

import com.sandy.capitalyst.server.CapitalystServer;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CookieUtil {

    public static Map<String, String> loadNSECookies() throws Exception {

        File cookieFile = new File( CapitalystServer.getConfig().getWorkspaceDir(),
                                    "cookies/nse-itd-cookie.properties" ) ;

        Map<String, String> cookies = new HashMap<>() ;
        if( cookieFile.exists() ) {
            Properties props = new Properties() ;
            props.load( new FileReader( cookieFile ) ) ;
            props.forEach( (key,value)-> {
                cookies.put( key.toString(), value.toString() ) ;
            }) ;
        }

        if( cookies.isEmpty() ) {
            throw new Exception( "NSE cookies could not be loaded." ) ;
        }

        return cookies ;
    }
}
