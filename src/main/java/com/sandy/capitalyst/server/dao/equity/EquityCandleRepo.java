package com.sandy.capitalyst.server.dao.equity;

import org.springframework.data.repository.CrudRepository ;

public interface EquityCandleRepo 
    extends CrudRepository<EquityCandle, Integer> {
}
