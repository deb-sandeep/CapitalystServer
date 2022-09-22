package com.sandy.capitalyst.server.dao.nvp.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.nvp.NVP ;

public interface NVPRepo extends CrudRepository<NVP, Integer> {
    
    public List<NVP> findByGroup( String groupName ) ;
    
    public List<NVP> findByName( String keyName ) ;

    public NVP findByGroupAndName( String groupName, String keyName ) ;
}
