package com.sandy.capitalyst.server.api.budget.vo;

import lombok.Getter ;

public abstract class BudgetLineItem {
    
    @Getter
    protected String lineItemName = null ;

    @Getter
    protected BudgetCell[] budgetCells = new BudgetCell[12] ;
    
    protected BudgetLineItem( String name ) {
        
        this.lineItemName = name ;
        
        for( int i=0; i<budgetCells.length; i++ ) {
            budgetCells[i] = new BudgetCell() ;
        }
    }

    public void addMonthlyCaps( int[] monthlyCaps ) {
        for( int i=0; i<12; i++ ) {
            budgetCells[i].addPlannedAmount( monthlyCaps[i] );
        }
    }
}
