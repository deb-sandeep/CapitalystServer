package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

public interface EquityTxnRepo 
    extends CrudRepository<EquityTxn, Integer> {
    
    EquityTxn findByHash( String hash ) ;
    
    List<EquityTxn> findByHoldingIdOrderByTxnDateAsc( int holdingId ) ;
}
