package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

public interface EquityTechIndicatorRepo 
    extends CrudRepository<EquityTechIndicator, Integer> {

    EquityTechIndicator findByIsinAndName( String isin, String name ) ;
}
