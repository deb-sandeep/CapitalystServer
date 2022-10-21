package com.sandy.capitalyst.server.api.equity.graph.indicators ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;
import static org.ta4j.core.indicators.numeric.NumericIndicator.of ;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.ta4j.core.BarSeries ;
import org.ta4j.core.indicators.EMAIndicator ;
import org.ta4j.core.indicators.MACDIndicator ;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator ;
import org.ta4j.core.indicators.numeric.NumericIndicator ;

import com.sandy.capitalyst.server.api.equity.graph.internal.AbstractIndicatorController ;
import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;

@RestController
public class MACDController extends AbstractIndicatorController {

    private static final Logger log = Logger.getLogger( MACDController.class ) ;
    
    @GetMapping( "/Equity/GraphData/Indicator/MACD" ) 
    public ResponseEntity<Map<String, Double[]>> getMACD( 
            @RequestParam( name="symbolNse",     required=true ) String  symbolNse,
            @RequestParam( name="minWindowSize", required=true ) Integer minWindowSize,
            @RequestParam( name="maxWindowSize", required=true ) Integer maxWindowSize,
            @RequestParam( name="sigWindowSize", required=true ) Integer sigWindowSize ) {
        
        try {
            ClosePriceIndicator cpInd      = null ;
            MACDIndicator       macdInd    = null ;
            EMAIndicator        sigInd     = null ;
            NumericIndicator    macdNumInd = null ;
            NumericIndicator    sigNumInd  = null ;
            NumericIndicator    histInd    = null ;
            
            log.debug( "Symbol NSE      = " + symbolNse  ) ;
            log.debug( "Min window size = " + minWindowSize ) ;
            log.debug( "Max window size = " + maxWindowSize ) ;
            log.debug( "Sig window size = " + sigWindowSize ) ;
            
            BarSeries series = BarSeriesCache.instance().get( symbolNse ) ;
            
            cpInd   = new ClosePriceIndicator( series ) ;
            macdInd = new MACDIndicator( cpInd, minWindowSize, maxWindowSize ) ;
            sigInd  = new EMAIndicator( macdInd, sigWindowSize ) ;
            
            macdNumInd = NumericIndicator.of( macdInd ) ;
            sigNumInd  = NumericIndicator.of( sigInd ) ;
            histInd    = macdNumInd.minus( sigNumInd ) ;
            
            Double[] lineValues = getValues( of( macdNumInd )) ;
            Double[] signValues = getValues( of( sigInd     )) ;
            Double[] histValues = getValues( histInd ) ;
            
            double scale = getHistScalingFactor( lineValues, signValues, histValues ) ;
            scale = Math.max( 1.0, scale ) ;
            for( int i=0; i<histValues.length; i++ ) {
                histValues[i] *= scale ;
            }
            
            Map<String, Double[]> seriesMap = new HashMap<>() ;
            seriesMap.put( "macd-line"  , lineValues ) ;
            seriesMap.put( "macd-signal", signValues ) ;
            seriesMap.put( "macd-hist",   histValues ) ;
            
            return status( OK ).body( seriesMap ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    private double getHistScalingFactor( Double[] arrayA, Double[] arrayB,
                                        Double[] histArray ) {
        
        List<Double> listA = Arrays.asList( arrayA ) ;
        List<Double> listB = Arrays.asList( arrayB ) ;
        List<Double> listH = Arrays.asList( histArray ) ;
        
        double maxA = Collections.max( listA ) ;
        double maxB = Collections.max( listB ) ;
        double maxH = Collections.max( listH ) ;
        
        double minA = Collections.min( listA ) ;
        double minB = Collections.min( listB ) ;
        double minH = Collections.min( listH ) ;
        
        double maxAB = Math.max( maxA, maxB ) ;
        double minAB = Math.min( minA, minB ) ;
        
        double plusYRatio = maxAB/maxH ;
        double negYRatio  = minAB/minH ;
        
        return Math.min( Math.abs( plusYRatio ), Math.abs( negYRatio ) ) ;
    }
}
