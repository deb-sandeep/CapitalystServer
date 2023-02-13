package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Calendar ;
import java.util.Date ;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.MonthlyLoadingCalculator ;
import com.sandy.capitalyst.server.dao.ledger.LedgerCategoryBudget ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

import lombok.Getter ;
import lombok.Setter ;

public class L2LineItem extends BudgetLineItem {
    
    private L1LineItem parent = null ;
    
    @Getter @Setter
    private LedgerCategoryBudget categoryBudget = null ;
    
    public L2LineItem( L1LineItem parent, LedgerCategoryBudget catBudget ) {
        
        super( catBudget.getL2CatName(), 
               parent.getStartOfYear(), 
               parent.getEndOfYear() ) ;
        
        this.parent = parent ;
        this.categoryBudget = catBudget ;
        
        createMontlyLoading() ;
    }
    
    private void createMontlyLoading() {
        
        MonthlyLoadingCalculator loadCalc = null ;
        String ruleSet = categoryBudget.getBudgetRule() ;
        
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
