package com.sandy.capitalyst.server.api.ledger;

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
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;

@RestController
public class LedgerClassificationRulesController {

    private static final Logger log = Logger.getLogger( LedgerClassificationRulesController.class ) ;
    
    @Autowired
    private LedgerEntryClassificationRuleRepo lecrRepo = null ;
    
    @GetMapping( "/Ledger/ClassificationRule" ) 
    public ResponseEntity<List<LedgerEntryClassificationRule>> getRules() {
        try {
            List<LedgerEntryClassificationRule> rules = null ;
            rules = lecrRepo.findAllRules() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( rules ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/ClassificationRule" ) 
    public ResponseEntity<APIResponse> saveLedgerEntryCategories(
                            @RequestBody LedgerEntryClassificationRule rule ) {
        try {
            lecrRepo.save( rule ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
