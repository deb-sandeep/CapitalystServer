package com.sandy.capitalyst.server.test;

import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

public class Scratch {

    private static final Logger log = Logger.getLogger( Scratch.class ) ;
    
    public static void main( String[] args ) throws Exception {
        
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd" ) ;
        Date start = sdf.parse( "2022/01/01" ) ;
        Date end   = sdf.parse( "2022/09/29" ) ;
        Date date  = start ;
        
        StringBuilder sb = new StringBuilder() ;
        while( date.before( end ) ) {
            
            log.debug( "['" + sdf.format( date ) + "', " + (int)(10+Math.random()*10) + "]," ) ;
            date = DateUtils.addDays( date, 1 ) ;
        }
        
        log.debug( "\n" + sb ) ;
    }
}
