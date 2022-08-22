package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

public interface EquityTxnRepo 
    extends CrudRepository<EquityTxn, Integer> {
    
    List<EquityTxn> findByHoldingIdOrderByTxnDateAscActionAsc( int holdingId ) ;
    
    void deleteByHoldingId( Integer holdingId ) ;
    
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
    
}
