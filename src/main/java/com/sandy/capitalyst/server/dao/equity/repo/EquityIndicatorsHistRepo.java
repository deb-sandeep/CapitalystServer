package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicatorsHist ;

public interface EquityIndicatorsHistRepo 
    extends CrudRepository<EquityIndicatorsHist, Integer> {
 
    EquityIndicatorsHist findByIsinAndAsOnDate( String isin, Date asOnDate ) ;
}
