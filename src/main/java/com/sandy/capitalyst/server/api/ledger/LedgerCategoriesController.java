package com.sandy.capitalyst.server.api.ledger;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.ledger.helpers.CategoryMergeHelper ;
import com.sandy.capitalyst.server.api.ledger.helpers.ChangedCategorySaveHelper ;
import com.sandy.capitalyst.server.api.ledger.helpers.MergeLedgeEntryCategoriesInput ;
import com.sandy.capitalyst.server.api.ledger.helpers.loadcalc.MonthlyLoadingCalculator ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.ClassifiedLedgerEntriesCounter ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@RestController
public class LedgerCategoriesController {

    private static final Logger log = Logger.getLogger( LedgerCategoriesController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    @Autowired
    private LedgerEntryClassificationRuleRepo lecrRepo = null ;
    
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
    
    @GetMapping( "/Ledger/Categories/ClassificationCount" ) 
    public ResponseEntity<List<ClassifiedLedgerEntriesCounter>> 
                                         getLedgerEntryClassificationCounter() {
        try {
            List<ClassifiedLedgerEntriesCounter> allCounters = null ;
            
            allCounters = new ArrayList<>() ;
            
            allCounters.addAll( lRepo.countClassifiedCreditEntries() ) ;
            allCounters.addAll( lRepo.countClassifiedDebitEntries() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( allCounters ) ;
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
            ChangedCategorySaveHelper helper = null ;
            
            helper = new ChangedCategorySaveHelper( categories, lRepo, 
                                                    lecRepo, lecrRepo ) ;
            helper.save() ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/Categories/Merge" ) 
    public ResponseEntity<APIResponse> mergeLedgerEntryCategories(
                            @RequestBody MergeLedgeEntryCategoriesInput input ) {
        try {
            CategoryMergeHelper helper = null ;
            
            helper = new CategoryMergeHelper( input, lRepo, 
                                              lecRepo, lecrRepo ) ;
            helper.merge() ;
            
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
    
    @PostMapping( "/Ledger/Category/AmountLoading/Validate" ) 
    public ResponseEntity<Map<String, Object>> valiateRule( @RequestBody String ruleText ) {
        
        Map<String, Object> result = new HashMap<>() ;
        
        try {
            
            MonthlyLoadingCalculator calc = null ;
            calc = new MonthlyLoadingCalculator( ruleText ) ;
            
            result.put( "yearlyCap", calc.getYearlyCap() ) ;
            result.put( "monthlyCap", calc.getMonthlyCap() ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( result ) ;
        }
        catch( Exception e ) {
            //log.debug( "Error", e ) ;
            result.put( "message", e.getMessage() ) ;
            return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                                 .body( result ) ;
        }
    }

    
}
