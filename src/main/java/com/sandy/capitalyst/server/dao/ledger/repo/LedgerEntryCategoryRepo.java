package com.sandy.capitalyst.server.dao.ledger.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;

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
          + "    lec.creditClassification = :creditClass AND "
          + "    lec.l1CatName = :l1CatName AND "
          + "    lec.l2CatName = :l2CatName "
    )
    public LedgerEntryCategory findCategory( 
            @Param( "creditClass" ) boolean creditClass,
            @Param( "l1CatName"   ) String l1CatName,
            @Param( "l2CatName"   ) String l2CatName ) ;
               
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
    
    @Query( value =   
            "SELECT "
          + "    lec "
          + "FROM "
          + "    LedgerEntryCategory lec "
          + "WHERE "
          + "    lec.creditClassification = true AND "
          + "    lec.amountLoadingRule IS NOT NULL "
          + "ORDER BY "
          + "    lec.l1CatName ASC, "
          + "    lec.l2CatName ASC "
    )
    public List<LedgerEntryCategory> findBudgetedCategories() ;
}
