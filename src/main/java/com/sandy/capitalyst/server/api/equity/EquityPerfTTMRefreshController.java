package com.sandy.capitalyst.server.api.equity ;

import static com.sandy.capitalyst.server.core.api.APIResponse.serverError ;
import static com.sandy.capitalyst.server.core.api.APIResponse.success ;
import static com.sandy.capitalyst.server.core.util.IndentUtil.* ;

import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.job.equity.eodrefresh.EquityTTMPerfUpdater ;

@RestController
public class EquityPerfTTMRefreshController {

    private static final Logger log = Logger.getLogger( EquityPerfTTMRefreshController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    public static final String CFG_GRP_NAME    = "EquityPerfTTMUpdater" ;
    public static final String CFG_INCL_STOCKS = "incl_stocks" ;
    public static final String CFG_EXCL_STOCKS = "excl_stocks" ;
    
    class Config {
        List<String> inclStocks = null ;
        List<String> exclStocks = null ;
    }

    @Autowired
    private HistoricEQDataRepo histRepo = null ;
    
    @PostMapping( "/Equity/PerfTTMRefresh" ) 
    public ResponseEntity<APIResponse> refreshPerfTTM() {

        log.debug( "Executing Perf TTM refresh API." ) ;
        ip() ;
        
        try {
            Config cfg = loadConfig() ;
            List<HistoricEQData> todayCandles = getTodayCandles() ;
            
            if( todayCandles == null || todayCandles.isEmpty() ) {
                
                return serverError( "Could not obtain most recent EOD data." ) ;
            }
            else {
                
                EquityTTMPerfUpdater updater = new EquityTTMPerfUpdater() ;
                filterStocks( todayCandles, updater, cfg ) ;
                
                updater.updateTTMPerfMeasures() ;
            }
            return success() ;
        }
        catch( Exception e ) {
            
            log.error( "Error :: Refreshing perf TTM.", e ) ;
            return serverError( "Error. Msg = " + e.getMessage() ) ;
        }
        finally {
            i_clear() ;
        }
    }
    
    private Config loadConfig() { 
        
        log.debug( "Loading config" ) ;
        
        NVPConfigGroup nvpCfg = NVPManager.instance()
                                          .getConfigGroup( CFG_GRP_NAME ) ;
        Config cfg = new Config() ;
        cfg.inclStocks = nvpCfg.getListValue( CFG_INCL_STOCKS, "" ) ;
        cfg.exclStocks = nvpCfg.getListValue( CFG_EXCL_STOCKS, "" ) ;
        
        log.debug( i1() + "Included stocks = " + String.join( ",", cfg.inclStocks ) ) ;
        log.debug( i1() + "Excluded stocks = " + String.join( ",", cfg.exclStocks ) ) ;
        
        return cfg ;
    }
    
    private List<HistoricEQData> getTodayCandles() { 
        
        Date today = new Date() ;
        List<HistoricEQData> candles = null ;

        today = DateUtils.truncate( today, Calendar.DAY_OF_MONTH ) ;
        log.debug( "Getting EOD data nearest to " + SDF.format( today )  );
        
        candles = histRepo.getHistoricDataClosestInPastToDate( today ) ;
        log.debug( i1() + "Got " + candles.size() + " candles." ) ;
        
        if( !candles.isEmpty() ) {
            log.debug( i1() + "Most recent EOD date " 
                            + SDF.format( candles.get( 0 ).getDate() ) ) ;
        }
        return candles ;
    }

    private void filterStocks( List<HistoricEQData> todayCandles, 
                               EquityTTMPerfUpdater updater, Config cfg ) {

        log.debug( "Filtering stocks" ) ;

        todayCandles.forEach( c -> {
            
            if( shouldProcessSymbol( c.getSymbol(), cfg ) ) {

                log.debug( i1() + "Selecting " + c.getSymbol() ) ;
                updater.addTodayEODCandle( c ) ;
            }
        } ) ;

        log.debug( i1() + updater.getNumStocksForUpdate() 
                        + " stocks filtered" ) ;
    }

    private boolean shouldProcessSymbol( String symbol, Config cfg ) {
        
        // If no include stocks are specified, we include all, else any 
        // stock not in the include list is ignored.
        if( !cfg.inclStocks.isEmpty() ) {
            if( !cfg.inclStocks.contains( symbol ) ) {
                return false ;
            }
        }
        
        // If no exclude stocks are specified, we include all, else any
        // stock in the exclude stock is rejected
        if( !cfg.exclStocks.isEmpty() ) {
            if( cfg.exclStocks.contains( symbol ) ) {
                return false ;
            }
        }
        
        return true ;
    }
}
