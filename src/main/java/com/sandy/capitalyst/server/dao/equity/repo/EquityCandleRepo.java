package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityCandle ;

public interface EquityCandleRepo 
    extends CrudRepository<EquityCandle, Integer> {
}
