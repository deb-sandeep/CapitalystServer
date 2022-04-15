package com.sandy.capitalyst.server.api.budget.vo;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.MonthlyLoadingCalculator ;
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
}
