package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.equity.EquityDailyGain ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

public interface EquityDailyGainRepo 
    extends CrudRepository<EquityDailyGain, Integer> {
    
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

}
