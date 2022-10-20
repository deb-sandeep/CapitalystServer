package com.sandy.capitalyst.server.api.equity ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;
import static org.ta4j.core.indicators.numeric.NumericIndicator.of ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.ta4j.core.BarSeries ;
import org.ta4j.core.Indicator ;
import org.ta4j.core.indicators.EMAIndicator ;
import org.ta4j.core.indicators.MACDIndicator ;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade ;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator ;
import org.ta4j.core.indicators.numeric.NumericIndicator ;

import com.sandy.capitalyst.server.api.equity.helper.EquityGraphDataBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

@RestController
public class EquityGraphDataController {

    private static final Logger log = Logger.getLogger( EquityGraphDataController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private static Map<String, BarSeries> BAR_SERIES_CACHE = new HashMap<>() ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @GetMapping( "/Equity/GraphData" ) 
    public ResponseEntity<GraphData> getGraphData( 
                  @RequestParam( "duration" ) String durationKey,
                  @RequestParam( name="symbolNse", required=true ) String symbolNse,
                  @RequestParam( name="owner",     required=true ) String ownerName ) {
        try {
            
            EquityMaster em = emRepo.findBySymbol( symbolNse ) ;
            
            if( StringUtil.isEmptyOrNull( durationKey ) ) {
                durationKey = "6m" ;
            }
            
            Date toDate = new Date() ;
            Date fromDate = getFromDate( toDate, durationKey ) ;
            
            log.debug( "Fetching graph data" ) ;
            log.debug( "   Duration = " + durationKey ) ;
            log.debug( "   Symbol   = " + symbolNse   ) ;
            log.debug( "   Owner    = " + ownerName   ) ;
            log.debug( "   From Date= " + SDF.format( fromDate ) ) ;
            log.debug( "   To Date  = " + SDF.format( toDate ) ) ;
            
            EquityGraphDataBuilder builder   = null ;
            GraphData              graphData = null ;
            
            builder = new EquityGraphDataBuilder() ;
            graphData = builder.constructGraphData( fromDate, toDate, 
                                                    em, ownerName ) ;
            
            BAR_SERIES_CACHE.put( symbolNse, builder.getBarSeries() ) ;
            
            return status( OK ).body( graphData ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    @GetMapping( "/Equity/GraphData/Indicator/BollingerBands" ) 
    public ResponseEntity<Map<String, Double[]>> getBolingerBands( 
          @RequestParam( name="symbolNse",  required=true ) String symbolNse,
          @RequestParam( name="windowSize", required=true ) Integer windowSize,
          @RequestParam( name="numStdDev",  required=true ) Integer numStdDev ) {
        
        try {
            log.debug( "Symbol NSE  = " + symbolNse  ) ;
            log.debug( "Window Size = " + windowSize ) ;
            log.debug( "Num Std Dev = " + numStdDev  ) ;
            
            BarSeries series = BAR_SERIES_CACHE.get( symbolNse ) ;
            
            if( series == null ) {
                log.error( "Cached bar series not found." ) ;
                return status( INTERNAL_SERVER_ERROR ).body( null ) ;            
            }

            BollingerBandFacade bbf = null ;
            bbf = new BollingerBandFacade( series, windowSize, numStdDev ) ;
            
            Map<String, Double[]> seriesValues = new HashMap<>() ;
            seriesValues.put( "bollinger-upper" , getValues( bbf.upper()  )) ;
            seriesValues.put( "bollinger-lower" , getValues( bbf.lower()  )) ;
            seriesValues.put( "bollinger-middle", getValues( bbf.middle() )) ;
            
            return status( OK ).body( seriesValues ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
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
            
            BarSeries series = getBarSeries( symbolNse ) ;
            
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
    
    private BarSeries getBarSeries( String symbolNse ) {
        
        BarSeries series = BAR_SERIES_CACHE.get( symbolNse ) ;
        if( series == null ) {
            log.error( "Cached bar series not found." ) ;
            throw new IllegalStateException( "Cached bar series not found." ) ;     
        }
        return series ;
    }
    
    private Double[] getValues( NumericIndicator indicator ) {
        int numElements = indicator.getBarSeries().getBarCount() ;
        Double[] values = Indicator.toDouble( indicator, 0, numElements ) ;
        return values ;
    }
    
    private Date getFromDate( final Date toDate, final String key ) {
        
        Date   fromDate = null ;
        int    amount   = Integer.parseInt( key.substring( 0, 1 ) ) ;
        String duration = key.substring( 1 ) ;
        
        if( duration.equalsIgnoreCase( "m" ) ) {
            fromDate = DateUtils.addMonths( toDate, -1*amount ) ;
        }
        else if( duration.equalsIgnoreCase( "y" ) ) {
            fromDate = DateUtils.addYears( toDate, -1*amount ) ;
        }
        return fromDate ;
    }
}
