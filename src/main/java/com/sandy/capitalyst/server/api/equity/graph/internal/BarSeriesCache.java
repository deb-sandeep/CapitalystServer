package com.sandy.capitalyst.server.api.equity.graph.internal;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.ta4j.core.BarSeries ;

public class BarSeriesCache {
    
    private static final Logger log = Logger.getLogger( BarSeriesCache.class ) ;

    private static BarSeriesCache instance = null ;
    
    public static BarSeriesCache instance() {
        if( instance == null ) {
            instance = new BarSeriesCache() ;
        }
        return instance ;
    }
    
    private Map<String, BarSeries> seriesCache = new HashMap<>() ;
    
    public synchronized void put( String symbolNse, BarSeries series ) {
        seriesCache.put( symbolNse, series ) ;
    }
    
    public synchronized BarSeries get( String symbolNse ) {
        
        BarSeries series = seriesCache.get( symbolNse ) ;
        if( series == null ) {
            String msg = "Cached bar series for " + symbolNse + " not found." ;
            log.error( msg ) ;
            throw new IllegalStateException( msg ) ;     
        }
        return series ;
    }
}
