package com.sandy.capitalyst.server.api.equity.graph.indicators ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;
import static org.ta4j.core.indicators.numeric.NumericIndicator.of ;

import java.util.HashMap ;
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
            
            Map<String, Double[]> seriesMap = new HashMap<>() ;
            seriesMap.put( "macd-line"  , getValues( of( macdNumInd ))) ;
            seriesMap.put( "macd-signal", getValues( of( sigInd     ))) ;
            seriesMap.put( "macd-hist",   getValues( histInd )) ;
            
            return status( OK ).body( seriesMap ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
