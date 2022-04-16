package com.sandy.capitalyst.server.api.budget.vo;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.CalendarUtil ;

import lombok.Getter ;

public abstract class BudgetLineItem {
    
    @Getter
    private String lineItemName = null ;

    @Getter
    private BudgetCell[] budgetCells = new BudgetCell[12] ;
    
    @Getter
    private long totalPlanned = 0 ;
    
    @Getter
    private long totalConsumed = 0 ;
    
    protected BudgetLineItem( String name ) {
        
        this.lineItemName = name ;
        
        for( int i=0; i<budgetCells.length; i++ ) {
            budgetCells[i] = new BudgetCell( CalendarUtil.getMonthName( i ) ) ;
        }
    }

    public void addMonthlyCaps( int[] monthlyCaps ) {
        
        this.totalPlanned = 0 ;
        
        for( int i=0; i<12; i++ ) {
            
            budgetCells[i].addPlannedAmount( monthlyCaps[i] );
            totalPlanned  += budgetCells[i].getPlanned() ;
        }
    }
    
    public void addConsumed( int monthIndex, int amt ) {
        
        BudgetCell cell = this.budgetCells[ monthIndex ] ;
        cell.addConsumedAmount( amt ) ;
        totalConsumed += amt ;
    }
    
    public boolean hasBudgedExceedCell() {
        for( int i=0; i<budgetCells.length; i++ ) {
            if( budgetCells[i].hasExceededBudged() ) 
                return true ;
        }
        return false ;
    }
}
