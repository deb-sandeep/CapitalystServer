package com.sandy.capitalyst.server.dao.index.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.index.IndexMaster ;

public interface IndexMasterRepo 
    extends CrudRepository<IndexMaster, Integer> {
    
    public IndexMaster findByName( String name ) ;
    
    @Query( value =   
        "SELECT "
      + "    im "
      + "FROM "
      + "    IndexMaster im "
      + "WHERE "
      + "    im.histEnabled = TRUE "
      + "ORDER BY "
      + "    im.id ASC "
    )
    public List<IndexMaster> findEodEnabledIndexes() ;
}
