package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTTMPerf ;

public interface EquityTTMPerfRepo 
    extends CrudRepository<EquityTTMPerf, Integer>, 
            JpaRepository<EquityTTMPerf, Integer> {

    public EquityTTMPerf findBySymbolNse( String symbolNse ) ;
}
