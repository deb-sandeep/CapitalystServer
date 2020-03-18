package com.sandy.capitalyst.server.dao.mf.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.mf.MutualFundMaster ;

public interface MutualFundRepo 
    extends CrudRepository<MutualFundMaster, Integer> {
 
    public MutualFundMaster findByIsin( String isin ) ;
}
