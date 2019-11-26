package com.sandy.capitalyst.server.dao.ledger;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Propagation ;
import org.springframework.transaction.annotation.Transactional ;

public interface LedgerRepo 
    extends CrudRepository<LedgerEntry, Integer> {
    
    public LedgerEntry findByHash( String hash ) ;
    
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id = :accountId AND "
          + "    le.valueDate >= :startDate AND "
          + "    le.valueDate <= :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "accountId" ) Integer accountId,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id = :accountId AND "
          + "    le.valueDate >= :startDate AND "
          + "    le.valueDate <= :endDate AND "
          + "    le.amount >= :lowerAmt AND "
          + "    le.amount <= :upperAmt "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "accountId" ) Integer accountId,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate,
                                    @Param( "lowerAmt"  ) Float lowerAmt,
                                    @Param( "upperAmt"  ) Float upperAmt ) ; 

    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "UPDATE "
          + "    LedgerEntry le "
          + "SET "
          + "    le.l1Cat = :l1Cat, "
          + "    le.l2Cat = :l2Cat, "
          + "    le.notes = :notes "
          + "WHERE "
          + "    le.id in :idList "
    )
    public void updateClassification( @Param( "idList" ) Integer[] idList, 
                                      @Param( "l1Cat" ) String l1Cat,
                                      @Param( "l2Cat" ) String l2Cat,
                                      @Param( "notes" ) String notes ) ;
}
