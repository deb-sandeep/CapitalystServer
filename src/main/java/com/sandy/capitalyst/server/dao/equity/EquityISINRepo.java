package com.sandy.capitalyst.server.dao.equity;

import org.springframework.data.repository.CrudRepository ;

public interface EquityISINRepo 
    extends CrudRepository<EquityISIN, Integer> {
    
    public EquityISIN findByIsin( String isin ) ;
}
