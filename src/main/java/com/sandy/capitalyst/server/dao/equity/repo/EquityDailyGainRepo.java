package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.equity.EquityDailyGain ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

public interface EquityDailyGainRepo 
    extends CrudRepository<EquityDailyGain, Integer>, IDCompressor {
    
    public static interface SparklineData {
        public Integer getHoldingId() ;
        public Date getDate() ;
        public Float getDayChange() ;
    }
    
    public EquityDailyGain findByHoldingAndDate( EquityHolding holding, 
                                                 Date date ) ;

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    edg.pat "
          + "FROM "
          + "    equity_daily_gain edg "
          + "WHERE "
          + "    edg.holding_id = :holdingId "
          + "ORDER BY "
          + "    edg.date DESC "
          + "LIMIT 1"
    )
    public Float getLastPAT( @Param( "holdingId" ) Integer id ) ;

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    * "
          + "FROM "
          + "    equity_daily_gain edg "
          + "WHERE "
          + "    edg.holding_id = :holdingId "
          + "ORDER BY "
          + "    edg.date DESC "
          + "LIMIT 1"
    )
    public EquityDailyGain getLastEDG( @Param( "holdingId" ) Integer id ) ;

    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    edg.holding_id as holdingId, "
          + "    edg.date as date, "
          + "    edg.day_change as dayChange "
          + "FROM "
          + "    equity_daily_gain edg "
          + "WHERE "
          + "    edg.holding_id = :holdingId AND "
          + "    edg.date BETWEEN :startDate AND :endDate "
          + "ORDER BY "
          + "    edg.date DESC "
    )
    List<SparklineData> getSparklineData( 
                                  @Param( "holdingId" ) Integer holdingId,
                                  @Param( "startDate" ) Date startDate,
                                  @Param( "endDate"   ) Date endDate ) ;

    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   equity_daily_gain "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<EquityDailyGain> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   equity_daily_gain " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
