package com.sandy.capitalyst.server.api.ledger;

import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;
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

import com.sandy.capitalyst.server.api.ledger.helpers.LedgerSearchCriteria ;
import com.sandy.capitalyst.server.api.ledger.helpers.SplitDetails ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.DebitCreditAssocRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;
import com.sandy.common.util.StringUtil ;

import static org.springframework.http.ResponseEntity.* ;

@RestController
public class LedgerRestController {

    private static final Logger log = Logger.getLogger( LedgerRestController.class ) ;
    private static final SimpleDateFormat PIVOT_SDF = new SimpleDateFormat( "yyyy-MM" ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private AccountRepo aRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    @Autowired
    private DebitCreditAssocRepo dcaRepo = null ;
    
    @GetMapping( "/Ledger/PivotData" ) 
    public ResponseEntity<List<String[]>> getPivotDataEntries( 
                           @RequestParam( "startDate" ) 
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                           Date startDate,
                           
                           @RequestParam( "endDate" ) 
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                           Date endDate ) {
        try {
            List<String[]> entries = findPivotDataEntries( startDate, endDate ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    @GetMapping( "/Ledger/Entries" )
    public ResponseEntity<List<LedgerEntry>> getLedgerEntries( 
                @RequestParam( "selectCreditEntries" ) Boolean selectCreditEntries,
                @RequestParam( "l1CatName" ) String l1CatName,
                @RequestParam( "l2CatName" ) String l2CatName ) {
        
        try {
            List<LedgerEntry> entries = null ;
            if( selectCreditEntries ) {
                entries = lRepo.findCreditEntries( l1CatName, l2CatName ) ; 
            }
            else {
                entries = lRepo.findDebitEntries( l1CatName, l2CatName ) ; 
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    @GetMapping( "/Ledger/PaginatedDebitEntries" )
    public ResponseEntity<List<LedgerEntry>> getDebitEntries( 
                                @RequestParam( "refTxnId" ) Integer refTxnId,
                                @RequestParam( "offset" )   Integer offset,
                                @RequestParam( "numTxns" )  Integer numTxns ) {
        
        try {
            List<LedgerEntry> entries = null ;
            
            LedgerEntry le = lRepo.findById( refTxnId ).get() ;
            Date refDate = le.getValueDate() ;
            
            entries = lRepo.findDebitEntries( refDate, offset, numTxns ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    @GetMapping( "/Ledger/DebitEntriesForMonth" )
    public ResponseEntity<List<LedgerEntry>> getCreditLedgerEntriesForMonth( 
                                @RequestParam( "l1CatName" ) 
                                String l1CatName,
                                
                                @RequestParam( "l2CatName" )
                                String l2CatName,
                                
                                @RequestParam( "startOfMonth" ) 
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                Date startOfMonth ) {
        
        try {
            List<LedgerEntry> entries = null ;
            List<LedgerEntry> returnValue = new ArrayList<>() ;
            
            startOfMonth = DateUtils.truncate( startOfMonth, Calendar.DAY_OF_MONTH ) ;
            
            Date endOfMonth = DateUtils.addMonths( startOfMonth, 1 ) ;
            endOfMonth = DateUtils.addSeconds( endOfMonth, -1 ) ;
            
            log.debug( "Start of month = " + startOfMonth ) ;
            log.debug( "End of month   = " + endOfMonth   ) ;
            
            if( StringUtil.isNotEmptyOrNull( l2CatName ) ) {
                entries = lRepo.findDebitEntriesForPeriod( l1CatName, l2CatName, 
                                                           startOfMonth, 
                                                           endOfMonth ) ;
            }
            else {
                entries = lRepo.findDebitEntriesForPeriod( l1CatName, 
                                                           startOfMonth, 
                                                           endOfMonth ) ;
            }
            
            // The debit entries can be compensated by associated credit 
            // entries. Made the adjustments and add only those debit entries
            // which have valid debit amount (<0)
            if( entries != null && !entries.isEmpty() ) {
                entries.forEach( entry -> {
                    List<DebitCreditAssoc> creditAssocs = null ;
                    creditAssocs = dcaRepo.findByDebitTxnId( entry.getId() ) ;
                    
                    if( creditAssocs != null && !creditAssocs.isEmpty() ) {
                        creditAssocs.forEach( credit ->{
                            entry.setAmount( entry.getAmount() + credit.getAmount() ) ;
                        } ) ;
                    }
                    
                    if( entry.getAmount() < 0 ) {
                        returnValue.add( entry ) ;
                        log.debug( entry ) ;
                    }
                } ) ;
            }
            
            return status( HttpStatus.OK ).body( returnValue ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/Search" ) 
    public ResponseEntity<Map<String,List<?>>> findLedgerEntries( 
                         @RequestBody LedgerSearchCriteria searchCriteria ) {
        try {
            
            Map<String, List<?>> response = new HashMap<>() ;
            
            List<LedgerEntry> entries = null ;
            entries = searchEntries( searchCriteria ) ;
            entries = filterResultsByCustomRule( searchCriteria, entries ) ;
            response.put( "ledgerEntries", entries ) ;
            
            List<Integer> associatedIds = new ArrayList<>() ;
            associatedIds.addAll( dcaRepo.findDistinctDebitTxnId() ) ;
            associatedIds.addAll( dcaRepo.findDistinctCreditTxnId() ) ;
            response.put( "associatedTxnIds", associatedIds ) ;
            
            return status( HttpStatus.OK ).body( response ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    @DeleteMapping( "/Ledger/{id}" ) 
    public ResponseEntity<APIResponse> deleteLedgerEntry( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting ledger entry. " + id ) ;
            LedgerEntry entry = lRepo.findById( id ).get() ;
            
            lRepo.deleteById( id ) ;

            Account account = entry.getAccount() ;
            if( account.getAccountNumber().equals( "CASH@HOME" ) ) {
                Float balance = lRepo.computeCashAccountBalance( account.getId() ) ;
                account.setBalance( balance ) ;
                aRepo.save( account ) ;
            }
            
            return status( HttpStatus.OK ).
                   body( new APIResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    private List<String[]> findPivotDataEntries( Date startDate, Date endDate ) {
        
        List<LedgerEntry> lEntries = lRepo.findEntries( startDate, endDate ) ;
        List<String[]> pivotEntries = new ArrayList<>() ;
        
        for( LedgerEntry entry : lEntries ) {
            
            if( entry.getAmount() == 0 ) continue ;
            
            String[] tupule = new String[6] ;
            
            String l1Cat = entry.getL1Cat() ;
            if( StringUtil.isEmptyOrNull( l1Cat ) ) {
                l1Cat = "--UNCATEGORIZED--" ;
            }
            
            String l2Cat = entry.getL2Cat() ;
            if( StringUtil.isEmptyOrNull( l2Cat ) ) {
                l2Cat = "--UNCATEGORIZED--" ;
            }
            
            tupule[0] = entry.isCredit() ? "Income" : "Expense" ;
            tupule[1] = l1Cat ;
            tupule[2] = l2Cat ;
            tupule[3] = PIVOT_SDF.format( entry.getValueDate() ) ;
            tupule[4] = Float.toString( entry.getAmount() ) ;
            
            pivotEntries.add( tupule ) ;
        }
        return pivotEntries ;
    }
    
    @PostMapping( "/LedgerEntry/Split" ) 
    public ResponseEntity<APIResponse> splitLedgerEntries( 
                         @RequestBody SplitDetails splitDetails ) {
        try {
            log.debug( "Splitting ledger entry. Details = " + splitDetails );
            
            LedgerEntry entryBeingSplit = null ;
            LedgerEntry newEntry = null ;
            
            if( splitDetails.isNewClassifier() ) {
                saveNewClassifier( splitDetails.getL1Cat(), splitDetails.getL2Cat() ) ;
            }
            
            entryBeingSplit = lRepo.findById( splitDetails.getEntryId() ).get() ;
            newEntry = entryBeingSplit.split( splitDetails ) ;
            
            lRepo.save( entryBeingSplit ) ;
            lRepo.save( newEntry ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR )
                   .body( null ) ;
        }
    }
    
    
    private void saveNewClassifier( String l1Cat, String l2Cat ) {
        LedgerEntryCategory newCat = null ;
        newCat = new LedgerEntryCategory() ;
        newCat.setCreditClassification( false ) ;
        newCat.setL1CatName( l1Cat ) ;
        newCat.setL2CatName( l2Cat ) ;
        lecRepo.save( newCat ) ;
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
            
            results = lRepo.findEntries( sc.getAccountIds(),
                                         sc.getStartDate(),
                                         sc.getEndDate(),
                                         lowerLim, upperLim ) ;
        }
        
        if( results != null ) {
            
            Iterator<LedgerEntry> entries = null ;
            
            if( sc.isShowOnlyUnclassified() ) {
                for( entries = results.iterator(); entries.hasNext(); ) {
                    LedgerEntry entry = entries.next() ;
                    if( StringUtil.isNotEmptyOrNull( entry.getL1Cat() ) ) {
                        entries.remove() ;
                    }
                }
            }
            
            if( StringUtil.isNotEmptyOrNull( sc.getL1CatName() ) || 
                StringUtil.isNotEmptyOrNull( sc.getL2CatName() ) ) {
                
                for( entries = results.iterator(); entries.hasNext(); ) {
                    LedgerEntry entry = entries.next() ;
                    
                    if( StringUtil.isEmptyOrNull( entry.getL1Cat() ) ) {
                        entries.remove() ;
                        continue ;
                    }

                    if( !entry.isCredit() == sc.isCreditClassifier() ) {
                        entries.remove() ;
                        continue ;
                    }
                    
                    if( StringUtil.isNotEmptyOrNull( sc.getL1CatName() ) ) {
                        if( StringUtil.isNotEmptyOrNull( entry.getL1Cat() ) ) {
                            if( !sc.getL1CatName().equals( entry.getL1Cat() ) ) {
                                entries.remove() ;
                                continue ;
                            }
                        }
                    }
                    
                    if( StringUtil.isNotEmptyOrNull( sc.getL2CatName() ) ) {
                        if( StringUtil.isNotEmptyOrNull( entry.getL2Cat() ) ) {
                            if( !sc.getL2CatName().equals( entry.getL2Cat() ) ) {
                                entries.remove() ;
                                continue ;
                            }
                        }
                    }
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
            LEClassifierRule rule = ruleBuilder.buildClassifier( "Temp rule", 
                                                                 customRule ) ;
            
            for( Iterator<LedgerEntry> iter = entries.iterator(); iter.hasNext(); ) {
                LedgerEntry entry = iter.next() ;
                if( rule.getMatchResult( entry ) == null ) {
                    iter.remove() ;
                }
            }
        }
        
        return entries ;
    }
}
