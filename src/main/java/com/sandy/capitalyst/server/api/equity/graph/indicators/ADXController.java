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
import org.ta4j.core.indicators.adx.ADXIndicator ;
import org.ta4j.core.indicators.adx.MinusDIIndicator ;
import org.ta4j.core.indicators.adx.PlusDIIndicator ;
import org.ta4j.core.indicators.numeric.NumericIndicator ;

import com.sandy.capitalyst.server.api.equity.graph.internal.AbstractIndicatorController ;
import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;

@RestController
public class ADXController extends AbstractIndicatorController {

    private static final Logger log = Logger.getLogger( ADXController.class ) ;
    
    @GetMapping( "/Equity/GraphData/Indicator/ADX" ) 
    public ResponseEntity<Map<String, Double[]>> getRSI( 
            @RequestParam( name="symbolNse",  required=true ) String  symbolNse,
            @RequestParam( name="diWindowSize", required=true ) Integer diWindowSize,
            @RequestParam( name="adxWindowSize", required=true ) Integer adxWindowSize ) {
        
        BarSeries        barSeries  = null ;
        ADXIndicator     adxInd     = null ;
        PlusDIIndicator  plusDIInd  = null ;
        MinusDIIndicator minusDIInd = null ;
        
        Double[] adxLineValues     = null ;
        Double[] plusDILineValues  = null ;
        Double[] minusDILineValues = null ;
        
        log.debug( "Symbol NSE     = " + symbolNse  ) ;
        log.debug( "DI Window size = " + diWindowSize ) ;
        log.debug( "ADX Window size= " + adxWindowSize ) ;
        
        try {
            barSeries = BarSeriesCache.instance().get( symbolNse ) ;
            
            plusDIInd  = new PlusDIIndicator( barSeries, adxWindowSize ) ;
            minusDIInd = new MinusDIIndicator( barSeries, adxWindowSize ) ;
            adxInd     = new ADXIndicator( barSeries, diWindowSize, adxWindowSize ) ;
            
            adxLineValues     = getValues( NumericIndicator.of( adxInd     ) ) ;
            plusDILineValues  = getValues( NumericIndicator.of( plusDIInd  ) ) ;
            minusDILineValues = getValues( NumericIndicator.of( minusDIInd ) ) ;
            
            Map<String, Double[]> seriesMap = new HashMap<>() ;
            
            seriesMap.put( "adx-line", adxLineValues     ) ;
            seriesMap.put( "plus-di",  plusDILineValues  ) ;
            seriesMap.put( "minus-di", minusDILineValues ) ;
            
            return status( OK ).body( seriesMap ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting ADX indicator values.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
