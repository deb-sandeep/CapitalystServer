package com.sandy.capitalyst.server.api.equity ;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.api.equity.recoengine.Recommendation ;

// @Get - /Equity/Type 

@RestController
public class EquityRecommendationsController {

    private static final Logger log = Logger.getLogger( EquityRecommendationsController.class ) ;
    
    @GetMapping( "/Equity/Recommendations" ) 
    public ResponseEntity<Recommendation> getIndividualEquityHoldings() {
        
        log.debug( "Getting equity recommendations." ) ;

        try {
            RecoManager.instance() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
