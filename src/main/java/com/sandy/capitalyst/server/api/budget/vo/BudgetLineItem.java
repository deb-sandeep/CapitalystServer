package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;

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
    
    @Getter
    private Date startOfYear = null ;
    
    @Getter
    private Date endOfYear = null ;
    
    protected BudgetLineItem( String name,
                              Date startDate, Date endDate ) {
        
        this.lineItemName = name ;
        this.startOfYear  = startDate ;
        this.endOfYear    = endDate ;
        
        Date startOfMonth = null ;
        Calendar cal = Calendar.getInstance() ;
        
        cal.setTime( startDate ) ;
        cal = DateUtils.truncate( cal, Calendar.YEAR ) ;
        cal.set( Calendar.MONTH, Calendar.APRIL ) ;
        
        startOfMonth = cal.getTime() ;
        
        for( int i=0; i<budgetCells.length; i++ ) {
            
            budgetCells[i] = new BudgetCell( CalendarUtil.getMonthName( i ),
                                             startOfMonth ) ;
            
            startOfMonth = DateUtils.addMonths( startOfMonth, 1 ) ;
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
    
    public void computeBudgetOverflows() {
        
        int carryOver = 0 ;
        Date today = new Date() ;
        
        for( int i=0; i<budgetCells.length; i++ ) {
            
            BudgetCell cell = budgetCells[i] ;
            if( today.after( cell.getStartOfMonth() ) ) {
                cell.setCarryOver( carryOver ) ;
            }
            else {
                break ;
            }
            carryOver = cell.getRemaining() ;
        }
    }
}
