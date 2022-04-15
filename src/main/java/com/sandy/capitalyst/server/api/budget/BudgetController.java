package com.sandy.capitalyst.server.api.budget;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.budget.helper.BudgetSpreadBuilder ;
import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@RestController
public class BudgetController {

    private static final Logger log = Logger.getLogger( BudgetController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    @GetMapping( "/Budget/Spread/{fy}" ) 
    public ResponseEntity<BudgetSpread> getBudgetSpread( 
                                  @PathVariable( "fy") int financialYear ) {
        
        try {
            log.debug( "Generating budget spread for FY " + financialYear ) ;
            
            BudgetSpreadBuilder builder = new BudgetSpreadBuilder( lRepo, lecRepo ) ;
            BudgetSpread spread = builder.createBudgetSpread( financialYear ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( spread ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
