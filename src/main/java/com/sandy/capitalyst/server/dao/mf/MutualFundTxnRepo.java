package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import org.springframework.data.repository.CrudRepository ;

public interface MutualFundTxnRepo 
    extends CrudRepository<MutualFundTxn, Integer> {
    
    MutualFundTxn findByMfIdAndTxnDate( Integer mfId, Date txnDate ) ;
}
