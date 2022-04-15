package com.sandy.capitalyst.server.api.budget.vo;

import lombok.Data ;

@Data
public class BudgetCell {
    
    private String month = null ;
    private int planned = 0 ;
    private int consumed = 0 ;
    
    public BudgetCell( String monthName ) {
        this.month = monthName ;
    }
    
    public int getRemaining() {
        return planned - consumed ;
    }

    public void addPlannedAmount( int amt ) {
        this.planned += amt ;
    }

    public void addConsumed( int amtConsumed ) {
        this.consumed += amtConsumed ;
    }
}
