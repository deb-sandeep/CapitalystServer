package com.sandy.capitalyst.server.dao.equity;

import org.springframework.data.repository.CrudRepository ;

public interface EquityTxnRepo 
    extends CrudRepository<EquityTxn, Integer> {
    
    EquityTxn findByHash( String hash ) ;
}
