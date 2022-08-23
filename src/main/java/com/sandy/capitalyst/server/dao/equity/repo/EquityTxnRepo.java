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
    
    
}
