package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;

public interface HistoricEQDataRepo 
    extends CrudRepository<HistoricEQData, Integer> {
}
