package com.sandy.capitalyst.server.dao.equity;

import static org.apache.commons.lang.time.DateUtils.addDays ;
import static org.apache.commons.lang.time.DateUtils.addMonths ;

import java.util.Calendar ;
import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_ttm_perf" )
public class EquityTTMPerf {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String  symbolNse = null ;
    private Boolean gapsFilled = null ;
    
    @Column( precision=16, scale=2, name="current_price" )
    private float currentPrice = 0.0F ;

    @Column( precision=16, scale=2, name="perf_1d" )
    private Float perf1d = null ;

    @Column( precision=16, scale=2, name="perf_1w" )
    private float perf1w = 0.0F ;

    @Column( precision=16, scale=2, name="perf_2w" )
    private float perf2w = 0.0F ;

    @Column( precision=16, scale=2, name="perf_1m" )
    private float perf1m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_2m" )
    private float perf2m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_3m" )
    private float perf3m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_4m" )
    private float perf4m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_5m" )
    private float perf5m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_6m" )
    private float perf6m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_7m" )
    private float perf7m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_8m" )
    private float perf8m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_9m" )
    private float perf9m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_10m" )
    private float perf10m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_11m" )
    private float perf11m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_12m" )
    private float perf12m = 0.0F ;

    @Column( precision=16, scale=2, name="perf_fy" )
    private float perfFy = 0.0F ;
    
    public boolean genuineGapExists() {
        
        // If we have already filled the gaps, no genuine gaps exist.
        if( gapsFilled == null || !gapsFilled ) {
            if( getNumGaps() > 0 ) {
                Date firstGapDate = getFirstMilestoneGapDate() ;
                if( firstGapDate != null ) {
                    return true ;
                }
            }
        }
        return false ;
    }

    private int getNumGaps() {
        
        int numGaps = 0 ;
        
        if( perf1w  == 0.0F ) numGaps++ ;
        if( perf2w  == 0.0F ) numGaps++ ;
        if( perf1m  == 0.0F ) numGaps++ ;
        if( perf2m  == 0.0F ) numGaps++ ;
        if( perf3m  == 0.0F ) numGaps++ ;
        if( perf4m  == 0.0F ) numGaps++ ;
        if( perf5m  == 0.0F ) numGaps++ ;
        if( perf6m  == 0.0F ) numGaps++ ;
        if( perf7m  == 0.0F ) numGaps++ ;
        if( perf8m  == 0.0F ) numGaps++ ;
        if( perf9m  == 0.0F ) numGaps++ ;
        if( perf10m == 0.0F ) numGaps++ ;
        if( perf11m == 0.0F ) numGaps++ ;
        if( perf12m == 0.0F ) numGaps++ ;
        if( perfFy  == 0.0F ) numGaps++ ;
        
        return numGaps ;
    }
    
    private Date getFirstMilestoneGapDate() {
        
        Date today = new Date() ;
        
        if( perf1w  == 0.0F ) {
            return addDays( today, -7 ) ;
        }
        if( perf2w  == 0.0F ) {
            return addDays( today, -14 ) ;
        }
        if( perf1m  == 0.0F ) {
            return addMonths( today, -1 ) ;
        }
        if( perf2m  == 0.0F ) {
            return addMonths( today, -2 ) ;
        }
        if( perf3m  == 0.0F ) {
            return addMonths( today, -3 ) ;
        }
        if( perf4m  == 0.0F ) {
            return addMonths( today, -4 ) ;
        }
        if( perf5m  == 0.0F ) {
            return addMonths( today, -5 ) ;
        }
        if( perf6m  == 0.0F ) {
            return addMonths( today, -6 ) ;
        }
        if( perf7m  == 0.0F ) {
            return addMonths( today, -7 ) ;
        }
        if( perf8m  == 0.0F ) {
            return addMonths( today, -8 ) ;
        }
        if( perf9m  == 0.0F ) {
            return addMonths( today, -9 ) ;
        }
        if( perf10m == 0.0F ) {
            return addMonths( today, -10 ) ;
        }
        if( perf11m == 0.0F ) {
            return addMonths( today, -11 ) ;
        }
        if( perf12m == 0.0F ) {
            return addMonths( today, -12 ) ;
        }
        if( perfFy  == 0.0F ) {
            
            Calendar cal    = Calendar.getInstance() ;
            int      fyYear = cal.get( Calendar.YEAR ) ;
            int      month  = cal.get( Calendar.MONTH ) ;
            
            if( month >= Calendar.JANUARY && month <= Calendar.MARCH ) { 
                fyYear -= 1 ;
            }
            cal.set( fyYear, Calendar.APRIL, 1, 0, 0, 0 ) ;
            return cal.getTime() ;
        }      
        
        return null ;
    }
}