package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;

public interface HistoricEQDataMetaRepo 
    extends CrudRepository<HistoricEQDataMeta, Integer>,
            JpaRepository<HistoricEQDataMeta, Integer>{
 
    HistoricEQDataMeta findBySymbolNse( String symbolNse ) ;
}
