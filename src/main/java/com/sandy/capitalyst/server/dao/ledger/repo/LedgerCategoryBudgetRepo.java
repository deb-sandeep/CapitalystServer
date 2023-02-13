package com.sandy.capitalyst.server.dao.ledger.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.ledger.LedgerCategoryBudget ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;

public interface LedgerCategoryBudgetRepo 
    extends CrudRepository<LedgerCategoryBudget, Integer> {
    
    @Query( value =   
            "SELECT "
          + "    lcb "
          + "FROM "
          + "    LedgerCategoryBudget lcb "
          + "WHERE "
          + "    lcb.budgetRule IS NOT NULL AND "
          + "    lcb.budgetRule <> '' AND "
          + "    lcb.fy = :fy "
          + "ORDER BY "
          + "    lcb.category.l1CatName ASC, "
          + "    lcb.category.l2CatName ASC "
    )
    public List<LedgerCategoryBudget> findAllByFy(  @Param( "fy" ) int fy ) ;
    
    public LedgerCategoryBudget findByCategoryAndFy( LedgerEntryCategory cat, int fy ) ;
}
