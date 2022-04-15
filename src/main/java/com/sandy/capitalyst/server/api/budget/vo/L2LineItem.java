package com.sandy.capitalyst.server.api.budget.vo;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;

import lombok.Getter ;
import lombok.Setter ;

public class L2LineItem extends BudgetLineItem {
    
    @Getter @Setter
    private LedgerEntryCategory category = null ;
}
