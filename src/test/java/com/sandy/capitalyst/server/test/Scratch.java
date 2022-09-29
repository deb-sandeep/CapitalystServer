package com.sandy.capitalyst.server.test;

import org.apache.log4j.AppenderSkeleton ;
import org.apache.log4j.Logger ;
import org.apache.log4j.spi.LoggingEvent ;

public class Scratch {

    private static final Logger log = Logger.getLogger( Scratch.class ) ;
    
    static class MyAppender extends AppenderSkeleton {

        StringBuilder sb = new StringBuilder() ;
        
        @Override
        protected void append( LoggingEvent event ) {
            sb.append( event.getMessage() ).append( "\n" ) ;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean requiresLayout() {
            return false ;
        }
        
        public String toString() {
            return sb.toString() ;
        }
    }

    public static void main( String[] args ) throws Exception {
        
        MyAppender appender = new MyAppender() ;
        Logger.getRootLogger().addAppender( appender ) ;
        
        log.debug( "1" ) ;
        log.debug( "2" ) ;
        log.debug( "3" ) ;
        Logger.getRootLogger().removeAppender( appender ) ;
        log.debug( "4" ) ;
     
        log.debug( "->" + appender.toString() );
    }
}
