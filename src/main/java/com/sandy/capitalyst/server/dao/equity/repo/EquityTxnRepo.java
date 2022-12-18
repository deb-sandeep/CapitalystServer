package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

public interface EquityTxnRepo 
    extends CrudRepository<EquityTxn, Integer>, IDCompressor {
    
    List<EquityTxn> findByHoldingIdOrderByTxnDateAscActionAsc( int holdingId ) ;
    
    void deleteByHoldingId( Integer holdingId ) ;
    
    EquityTxn findByOrderIdAndTradeId( String orderId, String tradeId ) ;
    
    @Query( value =   
            "SELECT "
          + "    et "
          + "FROM "
          + "    EquityTxn et "
          + "WHERE "
          + "    et.action = 'Buy' AND "
          + "    et.holdingId = :holdingId AND "
          + "    et.txnDate BETWEEN :startDate AND :endDate "
          + "ORDER BY "
          + "    et.txnDate ASC "
    )
    List<EquityTxn> findBuyTxns( @Param( "holdingId" ) Integer holdingId,
                                 @Param( "startDate" ) Date startDate,
                                 @Param( "endDate"   ) Date endDate ) ;
    
    @Query( value =   
            "SELECT "
          + "    et "
          + "FROM "
          + "    EquityTxn et "
          + "WHERE "
          + "    et.action = :action AND "
          + "    et.holdingId = :holdingId AND "
          + "    et.quantity = :quantity AND "
          + "    et.txnDate BETWEEN :startDate AND :endDate AND "
          + "    et.orderId IS NULL "
    )
    List<EquityTxn> findMatchingTxns( @Param( "holdingId" ) Integer holdingId,
                                      @Param( "startDate" ) Date startDate,
                                      @Param( "endDate"   ) Date endDate,
                                      @Param( "action"    ) String action,
                                      @Param( "quantity"  ) int quantity ) ;
    
    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   equity_txn "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<EquityTxn> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   equity_txn " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}