package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;

public interface HistoricEQDataRepo 
    extends CrudRepository<HistoricEQData, Integer>,
            JpaRepository<HistoricEQData, Integer>{
    
    public static interface ClosePrice {
        public Date getDate() ;
        public String getSymbol() ;
        public float getClose() ;
    }
    
    List<HistoricEQData> findBySymbolAndDate( String symbol, Date date ) ;
    
    @Query( nativeQuery = true,
            value = 
              "SELECT "
            + "    date, symbol, close "
            + "FROM "
            + "    historic_eq_data "
            + "WHERE "
            + "    date = ( "
            + "       SELECT date "
            + "       FROM historic_eq_data "
            + "       WHERE date <= :date "
            + "       ORDER BY date DESC "
            + "       LIMIT 1 "
            + "    ) "
            + "ORDER BY "
            + "    date DESC, "
            + "    symbol ASC"
    )
    List<ClosePrice> getClosePriceNearestToDate( @Param("date") Date date ) ;
    
    @Query( nativeQuery = true,
            value = 
              "SELECT "
            + "    * "
            + "FROM "
            + "    historic_eq_data "
            + "WHERE "
            + "    date = ( "
            + "       SELECT date "
            + "       FROM historic_eq_data "
            + "       WHERE date > :date "
            + "       ORDER BY date ASC "
            + "       LIMIT 1 "
            + "    ) "
            + "ORDER BY "
            + "    symbol ASC"
    )
    List<HistoricEQData> getHistoricDataClosestInFutureToDate( @Param("date") Date date ) ;
    
    @Query( nativeQuery = true,
            value = 
              "SELECT "
            + "    * "
            + "FROM "
            + "    historic_eq_data "
            + "WHERE "
            + "    date = ( "
            + "       SELECT date "
            + "       FROM historic_eq_data "
            + "       WHERE date <= :date "
            + "       ORDER BY date DESC "
            + "       LIMIT 1 "
            + "    ) "
            + "ORDER BY "
            + "    symbol ASC"
    )
    List<HistoricEQData> getHistoricDataClosestInPastToDate( @Param("date") Date date ) ;
    
    @Query( nativeQuery = true,
            value = 
            "SELECT * "
          + "FROM historic_eq_data "
          + "WHERE symbol = :symbol "
          + "ORDER BY date ASC "
          + "LIMIT 1 "
    )
    HistoricEQData getEarliestRecord( @Param("symbol") String symbol ) ;
    
    @Query( value = 
            "SELECT h "
          + "FROM HistoricEQData h "
          + "WHERE "
          + "   h.symbol = :symbol AND "
          + "   h.date BETWEEN :fromDate AND :toDate "
          + "ORDER BY "
          + "   h.date ASC "
    )
    List<HistoricEQData> getHistoricData( @Param( "symbol"   ) String symbol,
                                          @Param( "fromDate" ) Date fromDate,
                                          @Param( "toDate"   ) Date toDate ) ;
}
