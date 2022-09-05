package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTechIndicatorHist ;

public interface EquityTechIndicatorHistRepo 
    extends CrudRepository<EquityTechIndicatorHist, Integer> {

    EquityTechIndicatorHist findByIsinAndNameAndAsOnDate( String isin, 
                                                          String name, 
                                                          Date asOnDate ) ;
}
