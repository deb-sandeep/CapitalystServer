package com.sandy.capitalyst.server.api.ledger;

import java.sql.Date ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.ledger.helpers.LedgerClassificationInput ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;
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
    
    @PostMapping( "/Ledger/Classification" ) 
    public ResponseEntity<APIMsgResponse> classifyLedgerEntries( 
                         @RequestBody LedgerClassificationInput input ) {
        try {
            log.debug( "Classifying ledger entries." ) ;
            log.debug( "Classification input = " + input ) ;
            
            processEntryClassification( input ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse( "Success" ) ) ;
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
            
            LedgerEntryClassificationRule rule = null ;
            
            rule = saveNewClassificationRule( input ) ;
            new LEClassifier().runClassification( rule ) ;
        }
        
        String notes = input.getNotes() ;

        if( StringUtil.isNotEmptyOrNull( notes ) ) {
            lRepo.updateClassificationAndNotes( input.getEntryIdList(), 
                                                input.getL1Cat(), 
                                                input.getL2Cat(),
                                                notes ) ;
        }
        else {
            lRepo.updateClassification( input.getEntryIdList(), 
                                        input.getL1Cat(), 
                                        input.getL2Cat() ) ;
        }
    }

    private LedgerEntryClassificationRule saveNewClassificationRule( 
                                          LedgerClassificationInput input ) {
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
        return newRule ;
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
