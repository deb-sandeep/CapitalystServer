package com.sandy.capitalyst.server.dao.index.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.index.HistoricIdxData ;

public interface HistoricIdxDataRepo 
    extends CrudRepository<HistoricIdxData, Integer> {
    
}
