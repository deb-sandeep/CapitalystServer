package com.sandy.capitalyst.server.api.budget.helper;

import java.util.List ;

import com.sandy.capitalyst.server.api.budget.vo.BudgetSpread ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public class BudgetSpreadBuilder {

    private LedgerRepo lRepo = null ;
    private LedgerEntryCategoryRepo lecRepo = null ;
        
    public BudgetSpreadBuilder( LedgerRepo lRepo, 
                                LedgerEntryCategoryRepo lecRepo ) {
        this.lRepo = lRepo ;
        this.lecRepo = lecRepo ;
    }

    public BudgetSpread createBudgetSpread() {
        List<LedgerEntryCategory> budgetedCategories = null ;
        budgetedCategories = lecRepo.findBudgetedCategories() ;
        
        return null ;
    }
}
