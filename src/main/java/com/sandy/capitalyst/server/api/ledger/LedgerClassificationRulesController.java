package com.sandy.capitalyst.server.api.ledger;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.ledger.helpers.RuleMatchCount ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@RestController
public class LedgerClassificationRulesController {

    private static final Logger log = Logger.getLogger( LedgerClassificationRulesController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
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
    public ResponseEntity<APIResponse> saveRule(
                            @RequestBody LedgerEntryClassificationRule rule ) {
        try {
            rule.setLastUpdate( new java.sql.Date( System.currentTimeMillis() ) ) ;
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

    @DeleteMapping( "/Ledger/ClassificationRule/{id}" ) 
    public ResponseEntity<APIResponse> deleteRule( @PathVariable Integer id ) {
        try {
            lecrRepo.deleteById( id ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/Ledger/ClassificationRule/Validate" ) 
    public ResponseEntity<APIResponse> valiateRule( @RequestBody String ruleText ) {
        try {
            LEClassifierRuleBuilder builder = new LEClassifierRuleBuilder() ;
            builder.buildClassifier( ruleText ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( e.getMessage() ) ) ;
        }
    }

    @GetMapping( "/Ledger/ClassificationRule/MatchingEntries/{id}" ) 
    public ResponseEntity<List<LedgerEntry>> getMatchingEntries(  
                @PathVariable Integer id ) {
        try {
            List<LedgerEntry> entries = null ;
            LedgerEntryClassificationRule rule = null ;
            LEClassifierRuleBuilder builder = null ;
            LEClassifierRule classifier = null ;
            
            Date today = new Date() ;
            Date oneYrPastDate = new Date( today.getTime() - 31536000000L ) ;
            
            entries = lRepo.findEntries( oneYrPastDate, today ) ;
            rule = lecrRepo.findById( id ).get() ;
            builder = new LEClassifierRuleBuilder() ;
            classifier = builder.buildClassifier( rule.getRuleText() ) ;
            
            Iterator<LedgerEntry> iter = entries.iterator() ;
            while( iter.hasNext() ) {
                LedgerEntry entry = iter.next() ;
                if( !classifier.isRuleMatched( entry ) ) {
                    iter.remove() ;
                }
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @GetMapping( "/Ledger/ClassificationRule/MatchingCounter" ) 
    public ResponseEntity<List<RuleMatchCount>> getMatchingCounts() {
        
        try {
            List<LedgerEntry> entries = null ;
            List<LedgerEntryClassificationRule> rules = null ;
            LEClassifierRuleBuilder builder = null ;
            LEClassifierRule classifier = null ;
            List<RuleMatchCount> counter = new ArrayList<>() ;
            
            Date today = new Date() ;
            Date oneYrPastDate = new Date( today.getTime() - 31536000000L ) ;
            
            entries = lRepo.findEntries( oneYrPastDate, today ) ;
            rules = lecrRepo.findAllRules() ;
            
            builder = new LEClassifierRuleBuilder() ;
            
            for( LedgerEntryClassificationRule rule : rules ) {
                
                int matchCount = 0 ;
                classifier = builder.buildClassifier( rule.getRuleText() ) ;
                
                for( LedgerEntry entry : entries ) {
                    if( classifier.isRuleMatched( entry ) ) {
                        matchCount++ ;
                    }
                }
                
                counter.add( new RuleMatchCount( rule.getId(), matchCount ) ) ;
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( counter ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    
    
}
