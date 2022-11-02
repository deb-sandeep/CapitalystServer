package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;

import lombok.Data ;

@Data
public class BudgetCell {
    
    private String monthName = null ;
    private Date startOfMonth = null ;
    
    private int planned = 0 ;
    private int consumed = 0 ;
    private int carryOver = 0 ;
    
    public BudgetCell( String monthName, Date startOfMonth ) {
        this.monthName = monthName ;
        this.startOfMonth = DateUtils.addHours( startOfMonth, 5 ) ;
        this.startOfMonth = DateUtils.addMinutes( this.startOfMonth, 30 ) ;
    }
    
    public int getRemaining() {
        return getAvailable() - consumed ;
    }
    
    public int getAvailable() {
        return planned + carryOver ;
    }

    public void addPlannedAmount( int amt ) {
        this.planned += amt ;
    }

    public void addConsumedAmount( int amtConsumed ) {
        this.consumed += amtConsumed ;
    }
    
    public boolean hasExceededBudged() {
        return consumed > planned ;
    }
}
