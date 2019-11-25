package com.sandy.capitalyst.server.api.ledger;

import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.dao.ledger.AccountLedgerRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

@RestController
public class LedgerRestController {

    private static final Logger log = Logger.getLogger( LedgerRestController.class ) ;
    
    @Autowired
    private AccountLedgerRepo alRepo = null ;
    
    @PostMapping( "/Ledger/Search" ) 
    public ResponseEntity<List<LedgerEntry>> findLedgerEntries( 
                         @RequestBody LedgerSearchCriteria searchCriteria ) {
        try {
            log.debug( "Searching for ledger entries." ) ;
            log.debug( "Search criteria = " + searchCriteria ) ;
            List<LedgerEntry> entries = null ;
            
            entries = alRepo.findEntriesByDateRange( 
                                        searchCriteria.getAccountId(),
                                        searchCriteria.getStartDate(),
                                        searchCriteria.getEndDate() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
