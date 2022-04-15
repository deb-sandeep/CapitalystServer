package com.sandy.capitalyst.server.api.budget.vo;

import lombok.Data ;

@Data
public class BudgetCell {
    
    private int planned = 0 ;
    private int consumed = 0 ;
    
    public int getRemaining() {
        return planned - consumed ;
    }

    public void addPlannedAmount( int amt ) {
        this.planned += amt ;
    }
}
