package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;

public interface EquityIndicatorsRepo 
    extends CrudRepository<EquityIndicators, Integer> {
 
    EquityIndicators findByIsin( String isin ) ;
    
    EquityIndicators findBySymbolNse( String symbolNse ) ;
    
    @Query( value = 
            "SELECT ei.symbolNse "
          + "FROM EquityIndicators ei "
          + "ORDER BY "
          + "   ei.symbolNse ASC "
    )
    List<String> findSymbols() ;
}
