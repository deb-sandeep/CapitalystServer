package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

public interface EquityTechIndicatorRepo 
    extends CrudRepository<EquityTechIndicator, Integer>, IDCompressor {

    EquityTechIndicator findByIsinAndName( String isin, String name ) ;

    List<EquityTechIndicator> findByIsin( String isin ) ;
    
    List<EquityTechIndicator> findBySymbolNse( String symbolNse ) ;

    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   equity_tech_indicator "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<EquityTechIndicator> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   equity_tech_indicator " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
