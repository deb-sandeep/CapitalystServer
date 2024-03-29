package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.equity.EquityTrade ;

public interface EquityTradeRepo 
    extends CrudRepository<EquityTrade, Integer>, IDCompressor {
    
    public static String NVP_KEY_LAST_TRADE_UPDATE_DATE = "last_equity_trade_update_date" ;
    
    public EquityTrade findByOrderId( String orderId ) ;
    
    @Query( value = 
            "SELECT t "
          + "FROM EquityTrade t "
          + "WHERE "
          + "   t.symbolIcici = :symbolIcici AND "
          + "   t.ownerName = :ownerName "
          + "ORDER BY "
          + "   t.tradeDate ASC "
    )
    public List<EquityTrade> findTrades( String symbolIcici, String ownerName ) ;
    
    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   equity_trade "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<EquityTrade> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   equity_trade " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
