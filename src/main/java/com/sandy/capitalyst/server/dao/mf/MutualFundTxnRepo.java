package com.sandy.capitalyst.server.dao.mf;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

public interface MutualFundTxnRepo 
    extends CrudRepository<MutualFundTxn, Integer> {
    
    MutualFundTxn findByHash( String hash ) ;
    
    List<MutualFundTxn> findByMfIdOrderByTxnDateAsc( Integer mfId ) ;
}
