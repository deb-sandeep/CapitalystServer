package com.sandy.capitalyst.server.daemon.equity.intraday;

import java.util.List ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDSnapshotService.ITDSnapshot ;
import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal.TradingHolidayCalendar ;
import com.sandy.capitalyst.server.daemon.util.EventRateMonitor ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQITDData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQITDDataRepo ;

@Component
public class EquityITDImporterDaemon extends Thread {

    private static final Logger log = Logger.getLogger( EquityITDImporterDaemon.class ) ;

    public static final String CFG_GRP_NAME = "EquityITDImporterDaemon" ;
    
    public static final String CFG_PAUSE_REFRESH_FLAG = "pause_refresh" ;
    public static final String CFG_LIVE_REFRESH_DELAY = "refresh_delay_secs" ;
    public static final String CFG_LIVE_REFRESH_RND   = "refresh_delay_random" ;
    public static final String CFG_PRINT_DEBUG_STMT   = "debug_enable" ;
    public static final String CFG_FORCE_MKT_OPEN     = "force_mkt_open" ;
    
    public static final int MIN_REFRESH_DELAY = 15 ;

    // In the last 300 seconds, if we have 5 exceptions or more, the rate
    // monitor will keep the threshold breached flag till the time the 
    // count goes down below 5
    private EventRateMonitor genericERM = new EventRateMonitor( 300, 5 ) ;
    
    @Autowired
    private TradingHolidayCalendar holidayCalendar = null ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private HistoricEQITDDataRepo itdRepo = null ;
    
    @Autowired
    private EquityITDSnapshotService snapshotService = null ;
    
    @Autowired
    private EquityLTPRepository ltpRepository = null ;
    
    private boolean pauseRefresh       = false ;
    private int     refreshDelay       = 30 ;
    private int     refreshDelayRandom = 15 ;
    private boolean debugEnable        = true ;
    private boolean forceMktOpen       = false ;
    
    public EquityITDImporterDaemon() {}

    public void run() {
        
        List<ITDSnapshot> snapshots = null ;
        
        while( true ) {
            try {
                refreshConfiguration() ;
                
                if( isMktOpen() && 
                    !genericERM.hasThresholdBreached() ) {
                    
                    if( pauseRefresh ) {
                        if( debugEnable ) {
                            log.debug( "Equity ITD refresh paused." ) ;
                        }
                        TimeUnit.SECONDS.sleep( refreshDelay ) ;
                    }
                    else {
                        if( debugEnable ) {
                            log.debug( "" ) ;
                            log.debug( "Getting Equity ITD snapshot" ) ;
                        }
                        snapshots = snapshotService.getSnapshots() ;
                        processSnapshots( snapshots ) ;
                        randomSleep() ;
                    }
                }
                else {
                    TimeUnit.MINUTES.sleep( 2 ) ;
                }
            }
            catch( InterruptedException e ) {
                // Don't worry about it.
            }
            catch( Exception e ) {
                
                log.error( "Unanticipated error.", e ) ;
                genericERM.registerEvent() ;
                
                try {
                    TimeUnit.SECONDS.sleep( 10 ) ;
                }
                catch( InterruptedException ie ) {
                    log.error( "CMP daemon exception stall interrrupted.", ie ) ;
                    break ;
                }
            }
        }
    }
    
    private void randomSleep() throws Exception {
        
        int rndSleep = (int)( Math.random()*refreshDelayRandom ) ;
        rndSleep *= Math.random() < 0.5 ? -1 : 1 ;
        int sleepDur = refreshDelay + rndSleep ;

        if( sleepDur < MIN_REFRESH_DELAY ) {
            sleepDur = MIN_REFRESH_DELAY ;
        }
        
        if( debugEnable ) {
            log.debug( "Sleeping for " + sleepDur + " sec." ) ;
        }
        TimeUnit.SECONDS.sleep( sleepDur ) ;
    }
    
    private void refreshConfiguration() {
        
        NVPManager nvpMgr = NVPManager.instance() ;
        NVPConfigGroup cfg = nvpMgr.getConfigGroup( CFG_GRP_NAME ) ; ;

        pauseRefresh       = cfg.getBoolValue( CFG_PAUSE_REFRESH_FLAG, pauseRefresh       ) ;
        refreshDelay       = cfg.getIntValue ( CFG_LIVE_REFRESH_DELAY, refreshDelay       ) ;
        refreshDelayRandom = cfg.getIntValue ( CFG_LIVE_REFRESH_RND,   refreshDelayRandom ) ;
        debugEnable        = cfg.getBoolValue( CFG_PRINT_DEBUG_STMT,   debugEnable        ) ;
        forceMktOpen       = cfg.getBoolValue( CFG_FORCE_MKT_OPEN,     forceMktOpen       ) ;
        
        // Hardening - Prevents accidental setting of refresh delay to a lower
        // value which will result in a tight loop and API rate threshold
        // breach.
        if( refreshDelay < MIN_REFRESH_DELAY ) {
            refreshDelay = MIN_REFRESH_DELAY ;
            cfg.setValue( CFG_LIVE_REFRESH_DELAY, MIN_REFRESH_DELAY ) ;
        }
        
        if( (refreshDelay - refreshDelayRandom) < 15 ) {
            refreshDelayRandom = 0 ;
            cfg.setValue( CFG_LIVE_REFRESH_RND, 0 ) ;
        }
    }
    
    private boolean isMktOpen() {
        if( forceMktOpen ) return true ;
        return holidayCalendar.isMarketOpenNow() ;
    }
    
    private void processSnapshots( List<ITDSnapshot> snapshots ) {
        
        if( snapshots == null || snapshots.isEmpty() ) return ;
        
        if( debugEnable ) {
            log.debug( "- Saving " + snapshots.size() + " EQ ITD snapshots" ) ;
        }
        
        int numSaved = 0 ;
        for( ITDSnapshot s : snapshots ) {
            
            HistoricEQITDData itd = null ;
            EquityMaster em = emRepo.findBySymbol( s.getSymbol() ) ;
            
            if( em != null ) {
                itd = itdRepo.getITDData( em.getId(), s.getTime() ) ;
                if( itd == null ) {
                    
                    itd = new HistoricEQITDData() ;
                    itd.setEmId           ( em.getId()      ) ;
                    itd.setTime           ( s.getTime()     ) ;
                    itd.setPrice          ( s.getPrice()    ) ;
                    itd.setChangeAmt      ( s.getChangeAmt()) ;
                    itd.setPChange        ( s.getPChange()  ) ;
                    itd.setTotalTradedVal ( s.getTotalVal() ) ;
                    itd.setTotalTradedVol ( s.getTotalVol() ) ;
                    
                    itdRepo.save( itd ) ;
                    ltpRepository.addSnapshot( s ) ;
                    
                    numSaved++ ;
                }
            }
        } ;
        
        if( debugEnable ) {
            log.debug( "-> Saved " + numSaved + " snapshots" ) ;
        }
    }
}
