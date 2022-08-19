package com.sandy.capitalyst.server.api.equity.market.recoengine;

import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

import lombok.Data ;

@Data
public abstract class Filter {
    
    @Data
    public static class FilterResult {
        
        public static final int ACCEPT = 0 ;
        public static final int REJECT = 0 ;
        
        private int result = ACCEPT ;
        private String description = null ;
        
        public FilterResult( int result, String description ) {
            this.result = result ;
            this.description = description ;
        }
    }

    private int priority = 0 ;
    private float lowerLimit = Float.NEGATIVE_INFINITY ;
    private float upperLimit = Float.POSITIVE_INFINITY ;
    
    protected boolean withinLimits( float val ) {
        return ( val >= lowerLimit ) && ( val <= upperLimit ) ;
    }
    
    protected FilterResult accept( String msg ) {
        return new FilterResult( FilterResult.ACCEPT, msg ) ;
    }
    
    protected FilterResult reject( String msg ) {
        return new FilterResult( FilterResult.REJECT, msg ) ;
    }
    
    protected String msg( String template, Object ...parameters ) {
        String retVal = template ;
        for( int i=0; i<parameters.length; i++ ) {
            Object param = parameters[i] ;
            if( param != null ) {
                retVal = template.replace( "{"+i+"}", param.toString() ) ;
            }
        }
        return retVal ;
    }
    
    public abstract FilterResult filter( EquityIndicators ind,
                                         List<EquityTechIndicator> techInds, 
                                         EquityRecommendations recos ) ;
}
