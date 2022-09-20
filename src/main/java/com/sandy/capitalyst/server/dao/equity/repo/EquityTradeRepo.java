package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTrade ;

public interface EquityTradeRepo 
    extends CrudRepository<EquityTrade, Integer> {
    
}
