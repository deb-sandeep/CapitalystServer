package com.sandy.capitalyst.server.api.budget.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.Calendar ;
import java.util.Date ;
import java.util.GregorianCalendar ;
import java.util.List ;
import java.util.TimeZone ;

import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;
import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.DebitCreditAssocRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public class BudgetSpreadBuilder {

    private LedgerRepo              lRepo   = null ;
    private LedgerEntryCategoryRepo lecRepo = null ;
    private DebitCreditAssocRepo    dcaRepo = null ;
        
    public BudgetSpreadBuilder() {
        this.lRepo   = getBean( LedgerRepo.class ) ;
        this.lecRepo = getBean( LedgerEntryCategoryRepo.class ) ;
        this.dcaRepo = getBean( DebitCreditAssocRepo.class ) ;
    }

    public BudgetSpread createBudgetSpread( int financialYear ) {
        
        Date startDate = getStartOfFY( financialYear ) ;
        Date endDate   = getEndOfFY( financialYear ) ;
        
        BudgetSpread spread = null ;
        List<LedgerEntry> debitEntries = null ;
        List<Integer> debitIdsWithRecoveries = null ;
        
        spread = buildBudgetSpread( financialYear, startDate, endDate ) ;
        debitEntries = lRepo.findDebitEntries( startDate, endDate ) ;
        debitIdsWithRecoveries = dcaRepo.findDistinctDebitTxnId() ;
        
        for( LedgerEntry debitLE : debitEntries ) {
            
            // Some or all of this debit entry amount has been recovered by
            // associated credit transactions. Adjust the debit amount to reflect
            // those recoveries. If the recovery is complete, we should ignore
            // processing those debit entries.
            if( debitIdsWithRecoveries.contains( debitLE.getId() ) ) {
                
                float recoveredAmt = getRecoveryAmount( debitLE.getId() ) ;
                float remainingAmt = debitLE.getAmount() + recoveredAmt ;
                
                LedgerEntry debitLEClone = ( LedgerEntry )debitLE.clone() ;
                debitLEClone.setId( debitLE.getId() ) ;
                debitLEClone.setHash( debitLE.getHash() ) ;
                debitLEClone.setAmount( remainingAmt ) ;
                
                debitLE = debitLEClone ;
            }
            
            // Account for any floating point round offs
            if( debitLE.getAmount() <= -1F ) {
                spread.processEntry( debitLE ) ;
            }
        }
        
        spread.computeBudgetOverflows() ;
        
        return spread ;
    }
    
    private float getRecoveryAmount( Integer debitId ) {
        
        float recoveredAmount = 0 ;
        List<DebitCreditAssoc> creditAssociations = null ;
        
        creditAssociations = dcaRepo.findByDebitTxnId( debitId ) ;
        for( DebitCreditAssoc association : creditAssociations ) {
            recoveredAmount += association.getAmount() ;
        }
        return recoveredAmount ;
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
        
        Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "GMT+10:30" ) ) ;
        cal.set( fy, Calendar.APRIL, 1, 0, 0, 0 ) ;
        return cal.getTime() ;
    }

    private Date getEndOfFY( int fy ) {
        
        Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "GMT+10:30" ) ) ;
        cal.set( fy+1, Calendar.MARCH, 31, 23, 59, 59 ) ;
        return cal.getTime() ;
    }
}
