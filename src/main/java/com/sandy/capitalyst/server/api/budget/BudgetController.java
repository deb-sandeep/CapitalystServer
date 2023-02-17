package com.sandy.capitalyst.server.api.budget;

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.budget.helper.BudgetSpreadBuilder ;
import com.sandy.capitalyst.server.api.budget.vo.BudgetRuleInput ;
import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;
import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.MonthlyLoadingCalculator ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.dao.ledger.LedgerCategoryBudget ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerCategoryBudgetRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.common.util.StringUtil ;

@RestController
public class BudgetController {

    private static final Logger log = Logger.getLogger( BudgetController.class ) ;
    
    @Autowired
    private LedgerCategoryBudgetRepo lcbRepo = null ;
    
    @Autowired
    private LedgerEntryCategoryRepo lecRepo = null ;
    
    @GetMapping( "/Budget/Spread/{fy}" ) 
    public ResponseEntity<BudgetSpread> getBudgetSpread( @PathVariable( "fy") int fy ) {
        
        try {
            log.debug( "Fetching budget spread for FY = " + fy ) ;

            BudgetSpreadBuilder builder = new BudgetSpreadBuilder() ;
            BudgetSpread spread = builder.createBudgetSpread( fy ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( spread ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Creatign budget spread.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @GetMapping( "/Budget/Rules/{fy}" ) 
    public ResponseEntity<List<LedgerCategoryBudget>> getBudgetRules( 
                                  @PathVariable( "fy") int fy ) {
        
        try {
            log.debug( "Fetching budget rules for FY = " + fy ) ;

            List<LedgerCategoryBudget> budgetRules = lcbRepo.findAllByFy( fy ) ;
            if( budgetRules.isEmpty() ) {
                budgetRules = lcbRepo.findAllByFy( 0 ) ;
                for( LedgerCategoryBudget budget : budgetRules ) {
                    LedgerCategoryBudget newBudget = new LedgerCategoryBudget() ;
                    newBudget.setCategory( budget.getCategory() ) ;
                    newBudget.setFy( fy ) ;
                    newBudget.setBudgetRule( budget.getBudgetRule() ) ;
                    newBudget.setYearlyCap( budget.getYearlyCap() ) ;
                    lcbRepo.save( newBudget ) ;
                }
                budgetRules = lcbRepo.findAllByFy( fy ) ;
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( budgetRules ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Creatign budget spread.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/Budget/Rule/Validate" ) 
    public ResponseEntity<Map<String, Object>> valiateRule( 
                                                @RequestBody String ruleText ) {
        
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

    @PostMapping( "/Budget/Rule" ) 
    public ResponseEntity<APIMsgResponse> saveRule( @RequestBody BudgetRuleInput input ) {
        
        MonthlyLoadingCalculator calc = null ;
        LedgerEntryCategory cat = null ;
        LedgerCategoryBudget budget = null ;
        
        try {
            
            cat = lecRepo.findById( input.getCatId() ).get() ;
            budget = lcbRepo.findByCategoryAndFy( cat, input.getFy() ) ;

            if( budget == null ) {
                budget = new LedgerCategoryBudget() ;
                budget.setFy( input.getFy() ) ;
                budget.setCategory( cat ) ;
            }
            
            budget.setBudgetRule( input.getBudgetRule() ) ;
            budget.setYearlyCap( 0 ) ;

            if( StringUtil.isNotEmptyOrNull( input.getBudgetRule() ) ) {
                calc = new MonthlyLoadingCalculator( input.getBudgetRule() ) ;
                budget.setYearlyCap( calc.getYearlyCap() ) ;
            }
            
            lcbRepo.save( budget ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIMsgResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.debug( "Error", e ) ;
            return APIMsgResponse.serverError( e.getMessage() ) ;
        }
    }
}
