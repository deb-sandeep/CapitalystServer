package com.sandy.capitalyst.server.api.equity.graph.indicators ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.ta4j.core.BarSeries ;
import org.ta4j.core.indicators.RSIIndicator ;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator ;
import org.ta4j.core.indicators.numeric.NumericIndicator ;

import com.sandy.capitalyst.server.api.equity.graph.internal.AbstractIndicatorController ;
import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;

@RestController
public class RSIController extends AbstractIndicatorController {

    private static final Logger log = Logger.getLogger( RSIController.class ) ;
    
    @GetMapping( "/Equity/GraphData/Indicator/RSI" ) 
    public ResponseEntity<Map<String, Double[]>> getRSI( 
            @RequestParam( name="symbolNse",  required=true ) String  symbolNse,
            @RequestParam( name="windowSize", required=true ) Integer windowSize ) {
        
        BarSeries           barSeries  = null ;
        ClosePriceIndicator cpInd      = null ;
        RSIIndicator        rsiInd     = null ;
        Double[]            lineValues = null ;
        
        log.debug( "Symbol NSE  = " + symbolNse  ) ;
        log.debug( "Window size = " + windowSize ) ;
        
        try {
            barSeries = BarSeriesCache.instance().get( symbolNse ) ;
            
            cpInd  = new ClosePriceIndicator( barSeries ) ;
            rsiInd = new RSIIndicator( cpInd, windowSize ) ;
            
            lineValues = getValues( NumericIndicator.of( rsiInd ) ) ;
            
            Map<String, Double[]> seriesMap = new HashMap<>() ;
            seriesMap.put( "rsi-line", lineValues ) ;
            
            return status( OK ).body( seriesMap ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
