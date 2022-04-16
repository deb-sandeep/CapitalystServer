package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Calendar ;
import java.util.Date ;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.MonthlyLoadingCalculator ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;

import lombok.Getter ;
import lombok.Setter ;

public class L2LineItem extends BudgetLineItem {
    
    private L1LineItem parent = null ;
    
    @Getter @Setter
    private LedgerEntryCategory category = null ;
    
    public L2LineItem( L1LineItem parent, LedgerEntryCategory cat ) {
        
        super( cat.getL2CatName() ) ;
        this.parent = parent ;
        this.category = cat ;
        
        createMontlyLoading() ;
    }
    
    private void createMontlyLoading() {
        
        MonthlyLoadingCalculator loadCalc = null ;
        String ruleSet = category.getAmountLoadingRule() ;
        
        loadCalc = new MonthlyLoadingCalculator( ruleSet ) ;
        int[] monthlyCaps = loadCalc.getMonthlyCap() ;
        
        super.addMonthlyCaps( monthlyCaps ) ;
        parent.addMonthlyCaps( monthlyCaps ) ;
    }

    public void processEntry( LedgerEntry entry ) {
        
        int amt = -1* (int)entry.getAmount() ;
        int month = getBudgetMonth( entry.getValueDate() ) ;
        
        addConsumed( month, amt ) ;
        parent.addConsumed( month, amt ) ;
        parent.spread.addConsumed( month, amt ) ;
    }
    
    private int getBudgetMonth( Date date ) {
        
        Calendar cal = Calendar.getInstance() ;
        cal.setTime( date ) ;
        
        int calMonth = cal.get( Calendar.MONTH ) ;
        int budgetMonth = 0 ;
        
        if( calMonth < 3 ) {
            budgetMonth = calMonth + 9 ;
        }
        else {
            budgetMonth = calMonth - 3 ;
        }
        
        return budgetMonth ;
    }
}
