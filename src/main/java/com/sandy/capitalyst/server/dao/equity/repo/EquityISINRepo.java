package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityISIN ;

public interface EquityISINRepo 
    extends CrudRepository<EquityISIN, Integer> {
    
    public EquityISIN findByIsin( String isin ) ;
    
    public EquityISIN findBySymbol( String symbol ) ;
}
