package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicatorsHist ;

public interface EquityIndicatorsHistRepo 
    extends CrudRepository<EquityIndicatorsHist, Integer>, IDCompressor {
 
    EquityIndicatorsHist findByIsinAndAsOnDate( String isin, Date asOnDate ) ;

    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   equity_indicators_hist "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<EquityIndicatorsHist> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   equity_indicators_hist " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
