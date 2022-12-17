package com.sandy.capitalyst.server.dao.equity;

import java.time.Duration ;
import java.time.ZoneId ;
import java.time.ZonedDateTime ;
import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;
import javax.persistence.TableGenerator ;

import org.ta4j.core.Bar ;
import org.ta4j.core.BaseBar ;
import org.ta4j.core.num.DecimalNum ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_eq_data" )
public class HistoricEQData implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "eodPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "historic_eq_data_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="eodPkGen" )
    private Integer id = null ;
    
    private String symbol = null ;
    private long   totalTradeQty = 0 ;
    private long   totalTrades = 0 ;
    private Date   date = null ;
    
    @Column( precision=16, scale=2 )
    private float open = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float high = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float low = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float close = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private Float prevClose = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float totalTradeVal = 0.0F ;
    
    public Bar toBar() {
        return BaseBar.builder()
                      .timePeriod( Duration.ofDays( 1 ) )
                      .endTime( ZonedDateTime.ofInstant( date.toInstant(), ZoneId.of( "Asia/Kolkata" ) ) )
                      .openPrice ( DecimalNum.valueOf( open  ) )
                      .highPrice ( DecimalNum.valueOf( high  ) )
                      .lowPrice  ( DecimalNum.valueOf( low   ) )
                      .closePrice( DecimalNum.valueOf( close ) )
                      .trades    ( totalTrades )
                      .volume    ( DecimalNum.valueOf( totalTradeQty ) )
                      .amount    ( DecimalNum.valueOf( totalTradeVal ) )
                      .build() ;
    }
    
    public float getPctChange() {
        return ((close-prevClose)/prevClose)*100 ;
    }
}