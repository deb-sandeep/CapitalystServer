package com.sandy.capitalyst.server.dao.mf.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.mf.HistoricMFData ;

public interface HistoricMFDataRepo 
    extends CrudRepository<HistoricMFData, Integer> {
}
