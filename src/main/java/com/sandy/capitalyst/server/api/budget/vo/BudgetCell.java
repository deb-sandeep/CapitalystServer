package com.sandy.capitalyst.server.api.budget.vo;

import java.util.Date ;

import lombok.Data ;

@Data
public class BudgetCell {
    
    private String monthName = null ;
    private Date startOfMonth = null ;
    
    private int planned = 0 ;
    private int consumed = 0 ;
    
    public BudgetCell( String monthName, Date startOfMonth ) {
        this.monthName = monthName ;
        this.startOfMonth = startOfMonth ;
    }
    
    public int getRemaining() {
        return planned - consumed ;
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
