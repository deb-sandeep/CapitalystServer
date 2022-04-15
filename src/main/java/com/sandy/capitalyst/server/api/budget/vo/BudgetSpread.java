package com.sandy.capitalyst.server.api.budget.vo;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Getter ;
import lombok.Setter ;

public class BudgetSpread extends BudgetLineItem {

    @Getter @Setter
    private List<L1LineItem> l1LineItems = new ArrayList<>() ;
}
