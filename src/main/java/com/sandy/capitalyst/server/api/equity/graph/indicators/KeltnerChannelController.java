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
import org.ta4j.core.indicators.keltner.KeltnerChannelFacade ;

import com.sandy.capitalyst.server.api.equity.graph.internal.AbstractIndicatorController ;
import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;

@RestController
public class KeltnerChannelController extends AbstractIndicatorController {

    private static final Logger log = Logger.getLogger( KeltnerChannelController.class ) ;
    
    @GetMapping( "/Equity/GraphData/Indicator/KeltnerChannel" ) 
    public ResponseEntity<Map<String, Double[]>> getKeltnerChannel( 
          @RequestParam( name="symbolNse", required=true ) String  symbolNse,
          @RequestParam( name="emaCount",  required=true ) Integer emaCount,
          @RequestParam( name="atrCount",  required=true ) Integer atrCount,
          @RequestParam( name="atrScale",  required=true ) Integer atrScale ) {
        
        try {
            KeltnerChannelFacade kcf = null ;
            
            log.debug( "Symbol NSE = " + symbolNse ) ;
            log.debug( "EMA count  = " + emaCount  ) ;
            log.debug( "ATR count  = " + atrCount  ) ;
            log.debug( "ATR scale  = " + atrScale  ) ;
            
            BarSeries series = BarSeriesCache.instance().get( symbolNse ) ;
            
            kcf = new KeltnerChannelFacade( series, emaCount, atrCount, atrScale ) ;
            
            Map<String, Double[]> seriesValues = new HashMap<>() ;
            seriesValues.put( "keltner-upper" , getValues( kcf.upper()  )) ;
            seriesValues.put( "keltner-lower" , getValues( kcf.lower()  )) ;
            seriesValues.put( "keltner-middle", getValues( kcf.middle() )) ;
            
            return status( OK ).body( seriesValues ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting Keltner channel values.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
