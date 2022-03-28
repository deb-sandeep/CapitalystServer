package com.sandy.capitalyst.server.api.ledger;

import java.util.Collections ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;

@RestController
public class LedgerCategoriesController {

    private static final Logger log = Logger.getLogger( LedgerCategoriesController.class ) ;
    
    //@Autowired
    //private LedgerRepo lRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    //@Autowired
    //private LedgerEntryClassificationRuleRepo leClassificationRuleRepo = null ;
    
    @GetMapping( "/Ledger/Categories" ) 
    public ResponseEntity<List<LedgerEntryCategory>> getLedgerEntryCategories() {
        try {
            List<LedgerEntryCategory> categories = null ;
            categories = lecRepo.findAllCategories() ;
            Collections.sort( categories ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( categories ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/Categories" ) 
    public ResponseEntity<APIResponse> saveLedgerEntryCategories(
                            @RequestBody List<LedgerEntryCategory> categories ) {
        try {
            for( LedgerEntryCategory cat : categories ) {
                log.debug( cat ) ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @GetMapping( "/Ledger/CashEntryCategories" ) 
    public ResponseEntity<List<LedgerEntryCategory>> 
                                     getLedgerEntryCategoriesForCashEntry() {
        try {
            List<LedgerEntryCategory> categories = null ;
            categories = lecRepo.findCategoriesForCashEntry() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( categories ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
