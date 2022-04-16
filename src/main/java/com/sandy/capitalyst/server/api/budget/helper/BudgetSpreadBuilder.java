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
        
        Date startDate = getStartOfFY( financialYear ) ;
        Date endDate   = getEndOfFY( financialYear ) ;
        
        BudgetSpread spread = null ;
        List<LedgerEntry> ledgerEntries = null ;
        
        spread = buildBudgetSpread( financialYear, startDate, endDate ) ;
        ledgerEntries = lRepo.findCreditEntries( startDate, endDate ) ;
        
        for( LedgerEntry entry : ledgerEntries ) {
            spread.processEntry( entry ) ;
        }
        
        spread.computeBudgetOverflows() ;
        
        return spread ;
    }
    
    private BudgetSpread buildBudgetSpread( int fy, Date startDate, Date endDate ) {
        
        BudgetSpread spread = new BudgetSpread( startDate, endDate ) ;
        
        List<LedgerEntryCategory> budgetedCategories = null ;
        budgetedCategories = lecRepo.findBudgetedCategories() ;
        
        for( LedgerEntryCategory cat : budgetedCategories ) {
            spread.addCategory( cat ) ;
        }
        
        return spread ;
    }
    
    private Date getStartOfFY( int fy ) {
        
        Calendar cal = new GregorianCalendar() ;
        cal.set( fy, Calendar.APRIL, 1, 0, 0, 0 ) ;
        return cal.getTime() ;
    }

    private Date getEndOfFY( int fy ) {
        
        Calendar cal = new GregorianCalendar() ;
        cal.set( fy+1, Calendar.MARCH, 31, 23, 59, 59 ) ;
        return cal.getTime() ;
    }
}
