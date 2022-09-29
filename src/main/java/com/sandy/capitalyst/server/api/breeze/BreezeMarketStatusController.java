package com.sandy.capitalyst.server.api.breeze;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal.TradingHolidayCalendar ;

@RestController
public class BreezeMarketStatusController {

    private static final Logger log = Logger.getLogger( BreezeMarketStatusController.class ) ;
    
    private TradingHolidayCalendar holidayCalendar = null ;
    
    @GetMapping( "/Breeze/MarketStatus" ) 
    public ResponseEntity<Map<String, Boolean>> getMktStatus() {  
        
        try {
            if( holidayCalendar == null ) {
                holidayCalendar = new TradingHolidayCalendar() ;
            }

            Map<String, Boolean> result = new HashMap<>() ;
            result.put( "mktOpen", holidayCalendar.isMarketOpenNow() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( result ) ;
        }
        catch( Exception e ) {
            log.error( "Error in getting market status.", e ) ;
            
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
