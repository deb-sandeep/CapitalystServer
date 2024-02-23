package com.sandy.capitalyst.server.test;

import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;

public class Scratch {

    private static final Logger log = Logger.getLogger( Scratch.class ) ;
    
    private static final String I = "        " ;
    
    public static void main( String[] args ) throws Exception {

        try {
            functionB() ;
        }
        catch( Exception e ) {
            String str = ExceptionUtils.getStackTrace( e ) ;
            System.out.println( str ) ;
        }

    }

    public static void functionB() throws Exception {
        try {
            functionA();
        }
        catch ( Exception e ) {
            throw new IllegalStateException( "State", e ) ;
        }
    }

    public static void functionA() throws Exception {
        throw new IllegalArgumentException( "Illegal" ) ;
    }
}
