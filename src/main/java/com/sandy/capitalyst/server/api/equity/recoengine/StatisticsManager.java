package com.sandy.capitalyst.server.api.equity.recoengine;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;

import lombok.Getter ;

public class StatisticsManager {

    private static final Logger log = Logger.getLogger( StatisticsManager.class ) ;
    
    @Getter private DescriptiveStatistics mktCapStats = new DescriptiveStatistics() ;
    @Getter private float[] mktCapPctiles = new float[10] ;
    
    public void assimilate( EquityReco reco ) {
        
        EquityIndicators ind = reco.getIndicators() ;
        
        if( !reco.getEquityMaster().isEtf() ) {
            mktCapStats.addValue( ind.getMarketCap() ) ;
            aggregatePercentiles( mktCapStats, mktCapPctiles ) ;
        }
    }
    
    private void aggregatePercentiles( DescriptiveStatistics stats, 
                                       float[] pctileArray ) {
        for( int i=0; i<10; i++ ) {
            pctileArray[i] = (float)stats.getPercentile( i*10 + 10 ) ;
        }
    }
    
    public void printPercentiles() {
        
        printPercentiles( "Market cap", mktCapStats ) ;
    }
    
    private void printPercentiles( String title, 
                                   DescriptiveStatistics stats ) {
        
        log.debug( "Printing percentiles." ) ; 
        for( int i=10; i<=100; i+=10 ) {
            log.debug( "   " + i + " - " + stats.getPercentile( i ) ) ;
        }
    }
}
