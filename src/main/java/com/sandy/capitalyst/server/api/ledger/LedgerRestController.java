package com.sandy.capitalyst.server.api.ledger;

import java.util.Iterator ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.common.util.StringUtil ;

@RestController
public class LedgerRestController {

    private static final Logger log = Logger.getLogger( LedgerRestController.class ) ;
    
    @Autowired
    private LedgerRepo alRepo = null ;
    
    @PostMapping( "/Ledger/Search" ) 
    public ResponseEntity<List<LedgerEntry>> findLedgerEntries( 
                         @RequestBody LedgerSearchCriteria searchCriteria ) {
        try {
            log.debug( "Searching for ledger entries." ) ;
            log.debug( "Search criteria = " + searchCriteria ) ;
            
            List<LedgerEntry> entries = null ;
            entries = searchEntries( searchCriteria ) ;
            entries = filterResultsByCustomRule( searchCriteria, entries ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    private List<LedgerEntry> searchEntries( LedgerSearchCriteria sc ) {
        
        List<LedgerEntry> results = null ;
        
        if( sc.getMinAmt() == null && 
            sc.getMaxAmt() == null ) {
            
            results = alRepo.findEntries( sc.getAccountId(),
                                          sc.getStartDate(),
                                          sc.getEndDate() ) ;
        }
        else if( sc.getMinAmt() != null || 
                 sc.getMaxAmt() != null ) {
            
            Float lowerLim = sc.getMinAmt() == null ? 
                             -Float.MAX_VALUE : sc.getMinAmt() ;
            Float upperLim = sc.getMaxAmt() == null ?
                             Float.MAX_VALUE : sc.getMaxAmt() ;
            
            log.debug( "Lower limit = " + lowerLim ) ;
            log.debug( "Upper limit = " + upperLim ) ;
            
            results = alRepo.findEntries( sc.getAccountId(),
                                          sc.getStartDate(),
                                          sc.getEndDate(),
                                          lowerLim, upperLim ) ;
        }
        
        if( results != null && sc.isShowOnlyUnclassified() ) {
            for( Iterator<LedgerEntry> entries = results.iterator(); entries.hasNext(); ) {
                LedgerEntry entry = entries.next() ;
                if( StringUtil.isNotEmptyOrNull( entry.getL1Cat() ) ) {
                    entries.remove() ;
                }
            }
        }
        
        return results ;
    }
    
    private List<LedgerEntry> filterResultsByCustomRule( 
                LedgerSearchCriteria sc, List<LedgerEntry> entries ) {
        
        String customRule = sc.getCustomRule() ;
        if( StringUtil.isNotEmptyOrNull( customRule ) ) {
            LEClassifierRuleBuilder ruleBuilder = new LEClassifierRuleBuilder() ;
            LEClassifierRule rule = ruleBuilder.buildClassifier( customRule ) ;
            
            for( Iterator<LedgerEntry> iter = entries.iterator(); iter.hasNext(); ) {
                LedgerEntry entry = iter.next() ;
                if( !rule.isRuleMatched( entry ) ) {
                    iter.remove() ;
                }
            }
        }
        
        return entries ;
    }
}
