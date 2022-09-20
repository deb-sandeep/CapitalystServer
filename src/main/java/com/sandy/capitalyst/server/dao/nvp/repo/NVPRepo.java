package com.sandy.capitalyst.server.dao.nvp.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.nvp.NVP ;

public interface NVPRepo extends CrudRepository<NVP, Integer> {
    
    public NVP findByName( String name ) ;
}
