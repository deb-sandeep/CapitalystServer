package com.sandy.capitalyst.server.api.budget.helper;

import java.util.Calendar ;
import java.util.Date ;
import java.util.GregorianCalendar ;
import java.util.List ;

import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public class BudgetSpreadBuilder {

    private LedgerRepo lRepo = null ;
    private LedgerEntryCategoryRepo lecRepo = null ;
        
    public BudgetSpreadBuilder( LedgerRepo lRepo, 
                                LedgerEntryCategoryRepo lecRepo ) {
        this.lRepo = lRepo ;
        this.lecRepo = lecRepo ;
    }

    public BudgetSpread createBudgetSpread( int financialYear ) {
        
        BudgetSpread spread = buildBudgetSpread( financialYear ) ;
        List<LedgerEntry> ledgerEntries = findLedgerEntries( financialYear ) ;
        
        for( LedgerEntry entry : ledgerEntries ) {
            spread.processEntry( entry ) ;
        }
        
        return spread ;
    }
    
    private BudgetSpread buildBudgetSpread( int fy ) {
        
        BudgetSpread spread = new BudgetSpread( fy ) ;
        
        List<LedgerEntryCategory> budgetedCategories = null ;
        budgetedCategories = lecRepo.findBudgetedCategories() ;
        
        for( LedgerEntryCategory cat : budgetedCategories ) {
            spread.addCategory( cat ) ;
        }
        
        return spread ;
    }
    
    private List<LedgerEntry> findLedgerEntries( int fy ) {
        
        Calendar cal = new GregorianCalendar() ;
        cal.set( fy, Calendar.APRIL, 1, 0, 0, 0 ) ;
        Date startDate = cal.getTime() ;
        
        cal = new GregorianCalendar() ;
        cal.set( fy+1, Calendar.MARCH, 31, 0, 0, 0 ) ;
        Date endDate = cal.getTime() ;
        
        return lRepo.findCreditEntries( startDate, endDate ) ;
    }
}
