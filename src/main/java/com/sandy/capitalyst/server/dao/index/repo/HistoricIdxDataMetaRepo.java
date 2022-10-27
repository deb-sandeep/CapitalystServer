package com.sandy.capitalyst.server.dao.index.repo;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.index.HistoricIdxDataMeta ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;

public interface HistoricIdxDataMetaRepo 
    extends CrudRepository<HistoricIdxDataMeta, Integer>,
            JpaRepository<HistoricIdxDataMeta, Integer>{

    public HistoricIdxDataMeta findByIndex( IndexMaster im ) ;
}
