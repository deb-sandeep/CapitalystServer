package com.sandy.capitalyst.server.test;

import java.text.SimpleDateFormat ;

import org.apache.log4j.Logger ;

public class Scratch {

    private static final Logger log = Logger.getLogger( Scratch.class ) ;

    public static void main( String[] args ) throws Exception {
        
        SimpleDateFormat SDF = new SimpleDateFormat( "dd-MM-yyyy hh:mm:ss" ) ;
        log.debug( SDF.parse( "01-01-2001 00:00:00" ) );
    }
}
