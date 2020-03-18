package com.sandy.capitalyst.server.dao.ledger.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Propagation ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public interface LedgerRepo 
    extends CrudRepository<LedgerEntry, Integer> {
    
    public LedgerEntry findByHash( String hash ) ;
    
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.valueDate between :startDate AND :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id IN :accountIds AND "
          + "    le.valueDate between :startDate AND :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "accountIds" ) int[] accountIds,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id IN :accountIds AND "
          + "    le.valueDate between :startDate AND :endDate AND "
          + "    le.amount >= :lowerAmt AND "
          + "    le.amount <= :upperAmt "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "accountIds" ) int[] accountIds,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate,
                                    @Param( "lowerAmt"  ) Float lowerAmt,
                                    @Param( "upperAmt"  ) Float upperAmt ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id = :accountId AND "
          + "    le.valueDate between :startDate AND :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findEntries( 
                                    @Param( "accountId" ) int accountId,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
    
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
    
    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    le.balance "
          + "FROM "
          + "    account_ledger le "
          + "WHERE "
          + "    le.account_id = :accountId "
          + "ORDER BY "
          + "    le.value_date DESC, "
          + "    le.id DESC "
          + "LIMIT 1"
    )
    public Float getAccountBalance( @Param( "accountId" ) Integer accountId ) ;

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    SUM( le.amount ) "
          + "FROM "
          + "    account_ledger le "
          + "WHERE "
          + "    le.account_id = :accountId "
    )
    public Float computeCashAccountBalance( @Param( "accountId" ) Integer accountId ) ;
}
