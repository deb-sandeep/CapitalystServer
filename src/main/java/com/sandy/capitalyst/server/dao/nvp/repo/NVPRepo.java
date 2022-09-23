package com.sandy.capitalyst.server.dao.nvp.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.nvp.NVP ;

public interface NVPRepo extends CrudRepository<NVP, Integer> {
    
    public List<NVP> findByGroupName( String groupName ) ;
    
    public List<NVP> findByConfigName( String configName ) ;

    public NVP findByGroupNameAndConfigName( String groupName, 
                                             String configName ) ;
}
