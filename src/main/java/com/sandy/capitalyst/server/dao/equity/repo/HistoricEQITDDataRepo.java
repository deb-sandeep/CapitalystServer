package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.equity.HistoricEQITDData ;

public interface HistoricEQITDDataRepo 
    extends CrudRepository<HistoricEQITDData, Integer>{
    
    @Query( value = 
            "SELECT h "
          + "FROM HistoricEQITDData h "
          + "WHERE "
          + "   h.emId = :emId AND "
          + "   h.time = :time "
    )
    HistoricEQITDData getITDData( @Param( "emId" ) int emId, 
                                  @Param( "time" ) Date time ) ;
    
    @Query( nativeQuery = true,
            value = 
            "SELECT * " 
          + "FROM " 
          + "   historic_eq_itd_data itd "
          + "WHERE "
          + "   em_id = :emId "
          + "ORDER BY time DESC "
          + "LIMIT 1 "
    )
    HistoricEQITDData getLatestITDData( @Param( "emId" ) int emId ) ;
}
