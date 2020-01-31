package com.sandy.capitalyst.server.dao.equity;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

public interface EquityTxnRepo 
    extends CrudRepository<EquityTxn, Integer> {
    
    EquityTxn findByHash( String hash ) ;
    
    List<EquityTxn> findByHoldingIdOrderByTxnDateAsc( int holdingId ) ;
}
