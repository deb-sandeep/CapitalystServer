package com.sandy.capitalyst.server.api.equity.recoengine.evaluator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;

import lombok.Getter ;
import lombok.Setter ;

public abstract class Normalizer {
    
    @Getter @Setter
    private float weight = 0 ;
    
    @Getter
    private String name = null ;
    
    @Getter private float minNormVal = 0F ;
    @Getter private float avgNormVal = 0F ;
    @Getter private float maxNormVal = 0F ;
    
    public Normalizer( String name, float minNormVal, 
                       float avgNormVal, float maxNormVal ) {
        this.name = name ;
        this.minNormVal = minNormVal ;
        this.avgNormVal = avgNormVal ;
        this.maxNormVal = maxNormVal ;
    }

    public abstract float normalize( EquityReco reco, 
                                     StatisticsManager statsMgr ) ;
    
    protected float threePointNormalize( float xVal,
                                         DescriptiveStatistics stats ) {
        
        boolean arithmeticMean = false ;
        if( stats.getSortedValues()[0] < 0 ) {
            arithmeticMean = true ;
        }
        
        float min = (float)stats.getMin() ;
        float max = (float)stats.getMax() ;
        float avg = (float)( arithmeticMean  ? 
                             stats.getMean() : stats.getGeometricMean() ) ;
        
        float normVal = 0 ;
        
        if( xVal == avg ) {
            normVal = avg ;
        }
        else if( xVal < avg ) {
            float base = avg - min ;
            float height = avgNormVal - minNormVal ;
            
            normVal = (float)(( xVal - min )*height)/base ;
            normVal += minNormVal ;
        }
        else {
            float base = max - avg ;
            float height = maxNormVal - avgNormVal ;
            
            normVal = (float)(( xVal - avg)*height)/base ;
            normVal += avgNormVal ;
        }
        return normVal ;
    }
}
