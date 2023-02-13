package com.sandy.capitalyst.server.dao.ledger.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.ledger.LedgerCategoryBudget ;

public interface LedgerCategoryBudgetRepo 
    extends CrudRepository<LedgerCategoryBudget, Integer> {
    
    public List<LedgerCategoryBudget> findAllByFy( int fy ) ;
}
