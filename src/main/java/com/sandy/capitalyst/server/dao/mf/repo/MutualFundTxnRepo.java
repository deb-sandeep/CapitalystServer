package com.sandy.capitalyst.server.dao.mf.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.mf.MutualFundTxn ;

public interface MutualFundTxnRepo 
    extends CrudRepository<MutualFundTxn, Integer> {
    
    MutualFundTxn findByHash( String hash ) ;
    
    List<MutualFundTxn> findByMfIdOrderByTxnDateAsc( Integer mfId ) ;
}
