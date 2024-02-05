package com.sandy.capitalyst.server.dao.equity;

import java.time.Duration ;
import java.time.ZoneId ;
import java.time.ZonedDateTime ;
import java.util.Date ;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import org.ta4j.core.Bar ;
import org.ta4j.core.BaseBar ;
import org.ta4j.core.num.DecimalNum ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_eq_itd_data" )
public class HistoricEQITDData implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "itdPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "eq_itd_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="itdPkGen" )
    private Integer id = null ;
    
    private Integer emId = null ;
    private Date    time = null ;
    
    @Column( precision=16 )
    private float price = 0.0F ;
    
    @Column( precision=16 )
    private float changeAmt = 0.0F ;
    
    @Column( precision=16 )
    private float pChange = 0.0F ;
    
    @Column( precision=16 )
    private long totalTradedVol = 0 ;
    
    @Column( precision=16 )
    private float totalTradedVal = 0.0F ;
    
    public Bar toBar() {
        return 
        BaseBar.builder()
               .timePeriod( Duration.ofMinutes( 1 ) )
               .endTime   ( ZonedDateTime.ofInstant( time.toInstant(), ZoneId.of( "Asia/Kolkata" ) ) )
               .closePrice( DecimalNum.valueOf( price ) )
               .volume    ( DecimalNum.valueOf( totalTradedVol ) )
               .amount    ( DecimalNum.valueOf( totalTradedVol ) )
               .build() ;
    }
}
