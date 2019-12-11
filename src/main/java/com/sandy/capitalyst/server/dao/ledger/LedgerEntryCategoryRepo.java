package com.sandy.capitalyst.server.dao.ledger;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

public interface LedgerEntryCategoryRepo 
    extends CrudRepository<LedgerEntryCategory, Integer> {
    
    @Query( value =   
            "SELECT "
          + "    lec "
          + "FROM "
          + "    LedgerEntryCategory lec "
          + "ORDER BY "
          + "    lec.creditClassification ASC, "
          + "    lec.l1CatName ASC, "
          + "    lec.l2CatName ASC "
    )
    public List<LedgerEntryCategory> findAllCategories() ;
    
    @Query( value =   
            "SELECT "
          + "    lec "
          + "FROM "
          + "    LedgerEntryCategory lec "
          + "WHERE "
          + "    lec.validForCashEntry = true "
          + "ORDER BY "
          + "    lec.creditClassification ASC, "
          + "    lec.l1CatName ASC, "
          + "    lec.l2CatName ASC "
    )
    public List<LedgerEntryCategory> findCategoriesForCashEntry() ;
}
