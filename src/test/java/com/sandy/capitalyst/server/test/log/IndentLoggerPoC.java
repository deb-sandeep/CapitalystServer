package com.sandy.capitalyst.server.test.log;

import org.apache.log4j.Logger ;

public class IndentLoggerPoC {

    public static void main( String[] args ) {
        
        Logger log = Logger.getLogger( "Test" ) ;
        
        log.debug( "- Log 1" ) ;
        
        log.debug( ">! Log 1.1" ) ;
        log.debug( "> Log 1.1.1" ) ;
        log.debug( "> Log 1.1.1.1" ) ;
        log.debug( "> Log 1.1.1.1.1" ) ;
        log.debug( "- Log 1.1.1.1.2" ) ;
        
        log.debug( "<< Log 1.2" ) ;
        log.debug( "- Log 1.3" ) ;
        log.debug( "-> Temp" ) ;
        log.debug( "- Log 1.4" ) ;
    }
}
