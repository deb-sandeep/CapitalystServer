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
          + "    le.amount < 0 AND "
          + "    le.valueDate BETWEEN :startDate AND :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findCreditEntries( 
                                        @Param( "startDate" ) Date startDate,
                                        @Param( "endDate"   ) Date endDate ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.amount < 0 AND "
          + "    le.valueDate BETWEEN :startDate AND :endDate AND "
          + "    le.l1Cat = :l1CatName AND "
          + "    le.l2Cat = :l2CatName "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findCreditEntries( 
                                        @Param( "l1CatName" ) String l1CatName,
                                        @Param( "l2CatName" ) String l2CatName,
                                        @Param( "startDate" ) Date startDate,
                                        @Param( "endDate"   ) Date endDate ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.amount < 0 AND "
          + "    le.valueDate BETWEEN :startDate AND :endDate AND "
          + "    le.l1Cat = :l1CatName "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findCreditEntries( 
                                        @Param( "l1CatName" ) String l1CatName,
                                        @Param( "startDate" ) Date startDate,
                                        @Param( "endDate"   ) Date endDate ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.l1Cat = :l1CatName AND "
          + "    le.l2Cat = :l2CatName AND "
          + "    le.amount > 0 "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findCreditEntries( 
                                    @Param( "l1CatName" ) String l1CatName,
                                    @Param( "l2CatName" ) String l2CatName ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.l1Cat = :l1CatName AND "
          + "    le.l2Cat = :l2CatName AND "
          + "    le.amount < 0 "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findDebitEntries( 
                                    @Param( "l1CatName" ) String l1CatName,
                                    @Param( "l2CatName" ) String l2CatName ) ; 

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    * "
          + "FROM "
          + "    account_ledger le "
          + "WHERE "
          + "    le.id < ?1 AND "
          + "    le.amount < 0 "
          + "ORDER BY "
          + "    le.value_date DESC "
          + "LIMIT ?2"
    )
    public List<LedgerEntry> findNextDebitEntries( Integer refTxnId,
                                                   Integer numTxns ) ; 

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    * "
          + "FROM "
          + "    account_ledger le "
          + "WHERE "
          + "    le.id > ?1 AND "
          + "    le.amount < 0 "
          + "ORDER BY "
          + "    le.value_date ASC "
          + "LIMIT ?2"
    )
    public List<LedgerEntry> findPrevDebitEntries( Integer refTxnId,
                                                   Integer numTxns ) ; 

    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.valueDate BETWEEN :startDate AND :endDate "
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
          + "    le.valueDate BETWEEN :startDate AND :endDate AND "
          + "    ( le.l1Cat IS NULL AND le.l2Cat IS NULL ) "
          + "ORDER BY "
          + "    le.valueDate DESC, "
          + "    le.id DESC"
    )
    public List<LedgerEntry> findUnclassifiedEntries( 
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id IN :accountIds AND "
          + "    le.valueDate BETWEEN :startDate AND :endDate "
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
          + "    le.valueDate BETWEEN :startDate AND :endDate AND "
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
          + "    le.valueDate BETWEEN :startDate AND :endDate "
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
    public void updateClassificationAndNotes( 
                                      @Param( "idList" ) Integer[] idList, 
                                      @Param( "l1Cat" ) String l1Cat,
                                      @Param( "l2Cat" ) String l2Cat,
                                      @Param( "notes" ) String notes ) ;
    
    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "UPDATE "
          + "    LedgerEntry le "
          + "SET "
          + "    le.l1Cat = :l1Cat, "
          + "    le.l2Cat = :l2Cat "
          + "WHERE "
          + "    le.id in :idList "
    )
    public void updateClassification( @Param( "idList" ) Integer[] idList, 
                                      @Param( "l1Cat" ) String l1Cat,
                                      @Param( "l2Cat" ) String l2Cat ) ;
    
    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "UPDATE "
          + "    LedgerEntry le "
          + "SET "
          + "    le.l1Cat = :newL1Cat, "
          + "    le.l2Cat = :newL2Cat "
          + "WHERE "
          + "    le.l1Cat = :oldL1Cat AND "
          + "    le.l2Cat = :oldL2Cat AND "
          + "    le.amount > 0 "
    )
    public void updateCreditClassificationCategory(  
                                      @Param( "newL1Cat" ) String newL1Cat,
                                      @Param( "newL2Cat" ) String newL2Cat,
                                      @Param( "oldL1Cat" ) String oldL1Cat,
                                      @Param( "oldL2Cat" ) String oldL2Cat ) ;

    @Modifying( clearAutomatically = true )
    @Transactional( propagation = Propagation.REQUIRES_NEW )    
    @Query( value =   
            "UPDATE "
          + "    LedgerEntry le "
          + "SET "
          + "    le.l1Cat = :newL1Cat, "
          + "    le.l2Cat = :newL2Cat "
          + "WHERE "
          + "    le.l1Cat = :oldL1Cat AND "
          + "    le.l2Cat = :oldL2Cat AND "
          + "    le.amount < 0 "
    )
    public void updateDebitClassificationCategory(  
                                      @Param( "newL1Cat" ) String newL1Cat,
                                      @Param( "newL2Cat" ) String newL2Cat,
                                      @Param( "oldL1Cat" ) String oldL1Cat,
                                      @Param( "oldL2Cat" ) String oldL2Cat ) ;
    
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
    
    @Query( nativeQuery = true,
            value =   
              "SELECT "
            + "    if( amount < 0, 0, 1 ) as isCreditEntry, "
            + "    l1_cat as l1CatName, "
            + "    l2_cat as l2CatName, "
            + "    count(id) as numEntries "
            + "FROM  "
            + "    account_ledger "
            + "WHERE "
            + "    amount > 0 "
            + "GROUP BY "
            + "    l1_cat, l2_cat "
    )
    public List<ClassifiedLedgerEntriesCounter> countClassifiedCreditEntries() ;
    
    @Query( nativeQuery = true,
            value =   
              "SELECT "
            + "    if( amount < 0, 0, 1 ) as isCreditEntry, "
            + "    l1_cat as l1CatName, "
            + "    l2_cat as l2CatName, "
            + "    count(id) as numEntries "
            + "FROM  "
            + "    account_ledger "
            + "WHERE "
            + "    amount < 0 "
            + "GROUP BY "
            + "    l1_cat, l2_cat "
    )
    public List<ClassifiedLedgerEntriesCounter> countClassifiedDebitEntries() ;
}
