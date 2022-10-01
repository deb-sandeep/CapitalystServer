package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTrade ;

public interface EquityTradeRepo 
    extends CrudRepository<EquityTrade, Integer> {
    
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
}
