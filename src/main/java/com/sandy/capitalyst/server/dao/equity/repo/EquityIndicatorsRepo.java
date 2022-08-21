package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;

public interface EquityIndicatorsRepo 
    extends CrudRepository<EquityIndicators, Integer> {
 
    EquityIndicators findByIsin( String isin ) ;
    
    EquityIndicators findBySymbolNse( String symbolNse ) ;
}
