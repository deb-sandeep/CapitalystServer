package com.sandy.capitalyst.server.dao.ledger.repo;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Propagation ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;

public interface LedgerEntryClassificationRuleRepo 
    extends CrudRepository<LedgerEntryClassificationRule, Integer> {
    
    public LedgerEntry findByRuleName( String ruleName ) ;
    
    @Query( value =   
            "SELECT "
          + "    l "
          + "FROM "
          + "    LedgerEntryClassificationRule l "
          + "WHERE "
          + "    l.creditClassifier = :creditClass AND "
          + "    l.l1Category = :l1CatName AND "
          + "    l.l2Category = :l2CatName "
    )
    public LedgerEntryClassificationRule findRule( 
            @Param( "creditClass" ) boolean creditClass,
            @Param( "l1CatName"   ) String l1CatName,
            @Param( "l2CatName"   ) String l2CatName ) ;
    
    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "UPDATE "
          + "    LedgerEntryClassificationRule l "
          + "SET "
          + "    l.l1Category = :newL1Cat, "
          + "    l.l2Category = :newL2Cat "
          + "WHERE "
          + "    l.l1Category = :oldL1Cat AND "
          + "    l.l2Category = :oldL2Cat "
    )
    public void updateClassificationCategory(  
                                      @Param( "newL1Cat" ) String newL1Cat,
                                      @Param( "newL2Cat" ) String newL2Cat,
                                      @Param( "oldL1Cat" ) String oldL1Cat,
                                      @Param( "oldL2Cat" ) String oldL2Cat ) ;

    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "DELETE FROM "
          + "    LedgerEntryClassificationRule l "
          + "WHERE "
          + "    l.creditClassifier = :creditClass AND "
          + "    l.l1Category = :l1CatName AND "
          + "    l.l2Category = :l2CatName "
    )
    public void delete( @Param( "creditClass" ) boolean creditClass,
                        @Param( "l1CatName"   ) String l1CatName,
                        @Param( "l2CatName"   ) String l2CatName ) ;
}
