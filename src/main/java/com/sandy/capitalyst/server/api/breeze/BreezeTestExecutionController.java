package com.sandy.capitalyst.server.api.breeze;

import static org.apache.commons.lang.StringUtils.* ;

import java.io.StringWriter ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.apache.log4j.PatternLayout ;
import org.apache.log4j.WriterAppender ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;

@RestController
public class BreezeTestExecutionController {

    private static final Logger log = Logger.getLogger( BreezeTestExecutionController.class ) ;
    
    static class MyAppender extends WriterAppender {
        
        private static final String PATTERN = "%-5p %-4L %-30C{1} - %m%n";

        private final StringWriter stringWriter = new StringWriter() ;
        
        public MyAppender() {
            super() ;
            setWriter( stringWriter ) ;
            setLayout( new PatternLayout( PATTERN ) ) ;
            setThreshold( Level.TRACE ) ;
            activateOptions() ;
        }
        
        public String toString() {
            return stringWriter.toString() ;
        }
    }    

    @PostMapping( "/Breeze/Test/{userId}" ) 
    public ResponseEntity<Map<String, String>> testBreezeCall( @PathVariable String userId ) {  
        
        log.debug( "Received a breeze test request for " + userId ) ;
        
        MyAppender appender = new MyAppender() ;
        Logger.getRootLogger().addAppender( appender ) ;
        Map<String, String> results = new HashMap<>() ;
        
        try {
            BreezeCred cred = Breeze.instance().getCred( userId ) ;
            
            BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
            BreezeAPIResponse<PortfolioHolding> response = api.execute( cred ) ;
            
            log.debug( "" ) ;
            log.debug( "Symbol | Units |     CMP" ) ;
            log.debug( "-----------------------------" ) ;
            for( PortfolioHolding h : response.getEntities() ) {
                log.debug( rightPad( h.getSymbol(),           6 ) + " | " + 
                           leftPad ( ""+h.getQuantity(),      5 ) + " | " + 
                           leftPad ( ""+h.getAveragePrice(), 10 ) ) ;
            }
            
            results.put( "logs", appender.toString() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( results ) ;
        }
        catch( Exception e ) {
            
            log.error( "Error executing Breeze API", e ) ;
            
            results.put( "logs", appender.toString() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( results ) ;
        }
        finally {
            Logger.getRootLogger().removeAppender( appender ) ;
        }
    }
}
