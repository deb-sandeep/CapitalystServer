package com.sandy.capitalyst.server.core.util ;

import java.text.SimpleDateFormat ;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public final class StringUtil {
    
    public static final SimpleDateFormat DD_MM_YYYY  = new SimpleDateFormat( "dd-MM-yyyy" ) ;
    public static final SimpleDateFormat DD_MMM_YYYY = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    public static final SimpleDateFormat DD_MMM_YYYY_HH_MM_SS = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;

    public static boolean isEmptyOrNull( final String str ) {
        return ( str == null || "".equals( str.trim() ) ) ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
    
    public static String getHash( String input ) {
        return new String( Hex.encodeHex( DigestUtils.md5( input ) ) ) ;
    }
}
