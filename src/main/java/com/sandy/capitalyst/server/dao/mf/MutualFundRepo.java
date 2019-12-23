package com.sandy.capitalyst.server.dao.mf;

import org.springframework.data.repository.CrudRepository ;

public interface MutualFundRepo 
    extends CrudRepository<MutualFund, Integer> {
 
    public MutualFund findByIsin( String isin ) ;
}
