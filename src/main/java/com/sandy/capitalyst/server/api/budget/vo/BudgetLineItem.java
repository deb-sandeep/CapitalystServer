package com.sandy.capitalyst.server.api.budget.vo;

import lombok.Data ;

@Data
public abstract class BudgetLineItem {

    protected BudgetCell[] budgetCells = new BudgetCell[12] ;
    
    protected BudgetLineItem() {
        for( int i=0; i<budgetCells.length; i++ ) {
            budgetCells[i] = new BudgetCell() ;
        }
    }
}
