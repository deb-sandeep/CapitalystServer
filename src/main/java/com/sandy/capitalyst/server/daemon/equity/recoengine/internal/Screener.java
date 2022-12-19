package com.sandy.capitalyst.server.daemon.equity.recoengine.internal;

import java.util.List ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

import lombok.Data ;

@Data
public abstract class Screener {
    
    @Data
    public static class ScreenerResult {
        
        public static final int ACCEPT  = 0 ;
        public static final int REJECT  = 1 ;
        public static final int NO_CARE = 2 ;
        
        private int result = ACCEPT ;
        private String description = null ;
        
        public ScreenerResult( int result, String description ) {
            this.result = result ;
            this.description = description ;
        }
    }

    private String id = null ;
    private int priority = 0 ;
    private float lowerLimit = Float.NEGATIVE_INFINITY ;
    private float upperLimit = Float.POSITIVE_INFINITY ;
    
    protected boolean withinLimits( float val ) {
        return ( val >= lowerLimit ) && ( val <= upperLimit ) ;
    }
    
    protected ScreenerResult accept( String msg ) {
        return new ScreenerResult( ScreenerResult.ACCEPT, msg ) ;
    }
    
    protected ScreenerResult reject( String msg ) {
        return new ScreenerResult( ScreenerResult.REJECT, msg ) ;
    }
    
    protected ScreenerResult nocare() {
        return new ScreenerResult( ScreenerResult.NO_CARE, "Chaining forward." ) ;
    }
    
    protected String msg( String template, Object ...parameters ) {
        String retVal = template ;
        
        if( retVal.contains( "{id}" ) ) {
            retVal = retVal.replaceAll( "\\{id\\}", this.id ) ;
        }
        
        for( int i=0; i<parameters.length; i++ ) {
            Object param = parameters[i] ;
            if( param != null ) {
                retVal = retVal.replaceAll( "\\{"+i+"\\}", param.toString() ) ;
            }
        }
        return retVal ;
    }
    
    public abstract ScreenerResult screen( EquityIndicators ind,
                                           List<EquityTechIndicator> techInds, 
                                           EquityReco recos ) ;
    
    public void initialize() throws Exception {}
}
