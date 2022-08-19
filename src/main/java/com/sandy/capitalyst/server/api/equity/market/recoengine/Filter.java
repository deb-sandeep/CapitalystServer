package com.sandy.capitalyst.server.api.equity.market.recoengine;

import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

import lombok.Data ;

@Data
public abstract class Filter {

    private int priority = 0 ;
    private float lowerLimit = Float.NEGATIVE_INFINITY ;
    private float upperLimit = Float.POSITIVE_INFINITY ;
    
    protected boolean withinLimits( float val ) {
        return ( val >= lowerLimit ) && ( val <= upperLimit ) ;
    }
    
    public abstract void filter( EquityIndicators ind,
                                 List<EquityTechIndicator> techInds, 
                                 EquityRecommendations recos ) ;
}
