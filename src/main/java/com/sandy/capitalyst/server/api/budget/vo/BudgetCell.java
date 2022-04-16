package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Date ;

import lombok.Data ;

@Data
public class BudgetCell {
    
    private String monthName = null ;
    private transient Date startOfMonth = null ;
    
    private int planned = 0 ;
    private int consumed = 0 ;
    private int carryOver = 0 ;
    
    public BudgetCell( String monthName, Date startOfMonth ) {
        this.monthName = monthName ;
        this.startOfMonth = startOfMonth ;
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
