package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

public interface EquityHoldingRepo 
    extends CrudRepository<EquityHolding, Integer> {
    
    EquityHolding findByOwnerNameAndSymbolIcici( String ownerName, 
                                                 String symbolIcici ) ;
    
    List<EquityHolding> findByIsin( String isin ) ;
    
    List<EquityHolding> findBySymbolNse( String symbolNse ) ;

    @Query( value =   
            "SELECT "
          + "    eh "
          + "FROM "
          + "    EquityHolding eh "
          + "WHERE "
          + "    eh.quantity >0 "
    )
    List<EquityHolding> findNonZeroHoldings() ;
    
    @Query( value =   
            "SELECT "
          + "    eh "
          + "FROM "
          + "    EquityHolding eh "
          + "WHERE "
          + "    eh.quantity >0 AND "
          + "    eh.symbolNse = :symbolNse "
    )
    List<EquityHolding> findNonZeroHoldingsForNSESymbol( 
                                      @Param( "symbolNse" ) String symbolNse ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( 
            "UPDATE " 
          + "   EquityHolding eh "
          + "SET "
          + "   eh.isin = :isin " 
          + "WHERE "
          + "   eh.symbolNse =:symbolNse ")
    void updateISIN( @Param( "isin"      ) String isin, 
                     @Param( "symbolNse" ) String symbolNse ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( 
            "UPDATE " 
          + "   EquityHolding eh "
          + "SET "
          + "   eh.symbolNse = :symbolNse " 
          + "WHERE "
          + "   eh.isin =:isin ")
    void updateSymbolNSE( @Param( "isin"      ) String isin, 
                          @Param( "symbolNse" ) String symbolNse ) ;
    
    @Query( nativeQuery = true,
            value =   
            "SELECT " +
            "    distinct( txn.holding_id ) " +
            "FROM  " +
            "    equity_txn txn " +
            "WHERE " +
            "    txn.action = 'Sell' AND " +
            "    txn.txn_date BETWEEN :startDate AND :endDate"
    )
    List<Integer> getHoldingsSold( @Param( "startDate" ) Date startDate,
                                   @Param( "endDate"   ) Date endDate ) ;
    
    @Query( nativeQuery = true,
            value =   
            "SELECT " +
            "    distinct( txn.holding_id ) " +
            "FROM  " +
            "    equity_txn txn " +
            "WHERE " +
            "    txn.action = 'Buy' AND " +
            "    txn.txn_date BETWEEN :startDate AND :endDate"
    )
    List<Integer> getHoldingsBought( @Param( "startDate" ) Date startDate,
                                     @Param( "endDate"   ) Date endDate ) ;
}
