package com.sandy.capitalyst.server.api.ledger;

import java.sql.Date ;
import java.util.ArrayList ;
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
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.common.util.StringUtil ;

@RestController
public class LedgerClassificationRestController {

    private static final Logger log = Logger.getLogger( LedgerClassificationRestController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    @Autowired
    private LedgerEntryClassificationRuleRepo leClassificationRuleRepo = null ;
    
    @GetMapping( "/Ledger/Categories" ) 
    public ResponseEntity<List<LedgerEntryCategory>> getLedgerEntryCategories() {
        try {
            log.debug( "Getting ledger entry categories." ) ;
            List<LedgerEntryCategory> categories = new ArrayList<>() ;
            Iterable<LedgerEntryCategory> results = null ;
            
            results = lecRepo.findAll() ;
            for( LedgerEntryCategory result : results ) {
                categories.add( result ) ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( categories ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/Classification" ) 
    public ResponseEntity<APIResponse> classifyLedgerEntries( 
                         @RequestBody LedgerClassificationInput input ) {
        try {
            log.debug( "Classifying ledger entries." ) ;
            log.debug( "Classification input = " + input ) ;
            
            processEntryClassification( input ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( "Success" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    private void processEntryClassification( LedgerClassificationInput input ) {
        
        if( input.isNewClassifier() ) {
            saveNewClassifier( input ) ;
        }
        
        if( input.isSaveRule() && 
            StringUtil.isNotEmptyOrNull( input.getRule() ) ) {
            saveNewClassificationRule( input ) ;
        }
        
        lRepo.updateClassification( input.getEntryIdList(), 
                                    input.getL1Cat(), input.getL2Cat() ) ;
    }

    private void saveNewClassificationRule( LedgerClassificationInput input ) {
        log.debug( "Saving new classification rule" ) ;
        LedgerEntryClassificationRule newRule = null ;
        newRule = new LedgerEntryClassificationRule() ;
        newRule.setCreditClassifier( input.isCreditClassifier() ) ;
        newRule.setL1Category( input.getL1Cat() ) ;
        newRule.setL2Category( input.getL2Cat() ) ;
        newRule.setRuleText( input.getRule() ) ;
        newRule.setRuleName( input.getRuleName() ) ;
        newRule.setLastUpdate( new Date( System.currentTimeMillis() ) ) ;
        leClassificationRuleRepo.save( newRule ) ;
    }

    private void saveNewClassifier( LedgerClassificationInput input ) {
        log.debug( "Saving new classification category." ) ;
        LedgerEntryCategory newCat = null ;
        newCat = new LedgerEntryCategory() ;
        newCat.setCreditClassification( input.isCreditClassifier() ) ;
        newCat.setL1CatName( input.getL1Cat() ) ;
        newCat.setL2CatName( input.getL2Cat() ) ;
        lecRepo.save( newCat ) ;
    }
}
