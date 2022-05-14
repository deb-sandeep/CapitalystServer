package com.sandy.capitalyst.server.dao.index.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.index.IndexMaster ;

public interface IndexMasterRepo 
    extends CrudRepository<IndexMaster, Integer> {
    
    public IndexMaster findByName( String name ) ;
}
