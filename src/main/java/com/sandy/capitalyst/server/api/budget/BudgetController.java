package com.sandy.capitalyst.server.api.budget;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.budget.helper.BudgetSpreadBuilder ;
import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;

@RestController
public class BudgetController {

    private static final Logger log = Logger.getLogger( BudgetController.class ) ;
    
    @GetMapping( "/Budget/Spread/{fy}" ) 
    public ResponseEntity<BudgetSpread> getBudgetSpread( 
                                  @PathVariable( "fy") int financialYear ) {
        
        try {
            BudgetSpreadBuilder builder = new BudgetSpreadBuilder() ;
            BudgetSpread spread = builder.createBudgetSpread( financialYear ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( spread ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Creatign budget spread.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
