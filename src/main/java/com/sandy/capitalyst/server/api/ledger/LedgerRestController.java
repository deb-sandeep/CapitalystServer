package com.sandy.capitalyst.server.api.ledger;

import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.format.annotation.DateTimeFormat ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.common.util.StringUtil ;

@RestController
public class LedgerRestController {

    private static final Logger log = Logger.getLogger( LedgerRestController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private AccountRepo accountRepo = null ;
    
    @PostMapping( "/Ledger/Search" ) 
    public ResponseEntity<List<LedgerEntry>> findLedgerEntries( 
                         @RequestBody LedgerSearchCriteria searchCriteria ) {
        try {
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
    
    @GetMapping( "/Ledger/CashAtHome" )
    public ResponseEntity<List<LedgerEntry>> getCashAtHomeLedgerEntries(
                @RequestParam( name="fromDate" ) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Date fromDate,
                
                @RequestParam( name="toDate" ) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Date toDate ) {
        try {
            Account account = accountRepo.findByAccountNumber( "CASH@HOME" ) ;
            
            List<LedgerEntry> entries = null ;
            
            entries = lRepo.findEntries( account.getId(), fromDate, toDate ) ;
            for( LedgerEntry entry : entries ) {
                entry.setAccount( null ) ;
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
    
    private List<LedgerEntry> searchEntries( LedgerSearchCriteria sc ) {
        
        List<LedgerEntry> results = null ;
        
        if( sc.getMinAmt() == null && 
            sc.getMaxAmt() == null ) {
            
            results = lRepo.findEntries( sc.getAccountIds(),
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
            
            results = lRepo.findEntries( sc.getAccountIds(),
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

    @DeleteMapping( "/Ledger/{id}" ) 
    public ResponseEntity<APIResponse> deleteLedgerEntry( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting ledger entry. " + id ) ;
            lRepo.deleteById( id ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
