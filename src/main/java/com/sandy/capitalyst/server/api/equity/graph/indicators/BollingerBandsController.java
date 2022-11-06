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
import org.ta4j.core.indicators.bollinger.BollingerBandFacade ;

import com.sandy.capitalyst.server.api.equity.graph.internal.AbstractIndicatorController ;
import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;

@RestController
public class BollingerBandsController extends AbstractIndicatorController {

    private static final Logger log = Logger.getLogger( BollingerBandsController.class ) ;
    
    @GetMapping( "/Equity/GraphData/Indicator/BollingerBands" ) 
    public ResponseEntity<Map<String, Double[]>> getBolingerBands( 
          @RequestParam( name="symbolNse",  required=true ) String symbolNse,
          @RequestParam( name="windowSize", required=true ) Integer windowSize,
          @RequestParam( name="numStdDev",  required=true ) Integer numStdDev ) {
        
        try {
            BollingerBandFacade bbf = null ;
            
            log.debug( "Symbol NSE  = " + symbolNse  ) ;
            log.debug( "Window Size = " + windowSize ) ;
            log.debug( "Num Std Dev = " + numStdDev  ) ;
            
            BarSeries series = BarSeriesCache.instance().get( symbolNse ) ;
            
            bbf = new BollingerBandFacade( series, windowSize, numStdDev ) ;
            
            Map<String, Double[]> seriesValues = new HashMap<>() ;
            seriesValues.put( "bollinger-upper" , getValues( bbf.upper()  )) ;
            seriesValues.put( "bollinger-lower" , getValues( bbf.lower()  )) ;
            seriesValues.put( "bollinger-middle", getValues( bbf.middle() )) ;
            
            return status( OK ).body( seriesValues ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting Bollinger indicator values.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
