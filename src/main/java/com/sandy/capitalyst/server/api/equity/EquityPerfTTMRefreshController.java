package com.sandy.capitalyst.server.api.equity ;

import static com.sandy.capitalyst.server.core.api.APIMsgResponse.serverError ;
import static com.sandy.capitalyst.server.core.api.APIMsgResponse.success ;

import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityTTMPerfUpdater ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.core.log.IndentUtil ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityPerfTTMRefreshController {

    private static final Logger log = Logger.getLogger( EquityPerfTTMRefreshController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    @Autowired
    private HistoricEQDataRepo histRepo = null ;
    
    @PostMapping( "/Equity/PerfTTMRefresh" ) 
    public ResponseEntity<APIMsgResponse> refreshPerfTTM() {

        log.debug( "Executing Perf TTM refresh API. >" ) ;
        
        try {
            List<HistoricEQData> todayCandles = getTodayCandles() ;
            
            if( todayCandles == null || todayCandles.isEmpty() ) {
                return serverError( "Could not obtain most recent EOD data." ) ;
            }
            else {
                
                EquityTTMPerfUpdater updater = new EquityTTMPerfUpdater() ;
                
                log.debug( "- Filtering stocks for TTM update" ) ;
                todayCandles.forEach( updater::addTodayEODCandle ) ;
                
                updater.updateTTMPerfMeasures() ;
                
                RecoManager.instance().setEquityDataUpdated( true ) ;
            }
            return success() ;
        }
        catch( Exception e ) {
            
            log.error( "Error :: Refreshing perf TTM.", e ) ;
            return serverError( "Error. Msg = " + e.getMessage() ) ;
        }
        finally {
            IndentUtil.i_reset() ;
        }
    }
    
    private List<HistoricEQData> getTodayCandles() { 
        
        Date today = new Date() ;
        List<HistoricEQData> candles = null ;

        today = DateUtils.truncate( today, Calendar.DAY_OF_MONTH ) ;
        log.debug( "- Getting EOD data nearest to " + SDF.format( today )  );
        
        candles = histRepo.getHistoricDataClosestInPastToDate( today ) ;
        log.debug( "-> Got " + candles.size() + " candles." ) ;
        
        if( !candles.isEmpty() ) {
            log.debug( "-> Most recent EOD date " +
                       SDF.format( candles.get( 0 ).getDate() ) ) ;
        }
        return candles ;
    }
}
