package com.sandy.capitalyst.server.dao.mf;

import org.springframework.data.repository.CrudRepository ;

public interface MutualFundRepo 
    extends CrudRepository<MutualFundMaster, Integer> {
 
    public MutualFundMaster findByIsin( String isin ) ;
}
