package com.sandy.capitalyst.server.api.budget.vo;

import lombok.Data ;

@Data
public class BudgetRuleInput {

    private int catId = 0 ;
    private int fy = 0 ;
    private String budgetRule = null ;
}
