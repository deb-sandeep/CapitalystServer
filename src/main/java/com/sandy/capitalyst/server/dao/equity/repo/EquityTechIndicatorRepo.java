package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

public interface EquityTechIndicatorRepo 
    extends CrudRepository<EquityTechIndicator, Integer> {

    EquityTechIndicator findByIsinAndName( String isin, String name ) ;

    List<EquityTechIndicator> findByIsin( String isin ) ;
    
    List<EquityTechIndicator> findBySymbolNse( String symbolNse ) ;
}
