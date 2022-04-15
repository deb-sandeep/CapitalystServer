package com.sandy.capitalyst.server.api.budget.vo;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Getter ;
import lombok.Setter ;

public class L1LineItem extends BudgetLineItem {
    
    @Getter @Setter
    private List<L2LineItem> l2LineItems = new ArrayList<>() ;
    
    
}
