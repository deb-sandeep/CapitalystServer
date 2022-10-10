package com.sandy.capitalyst.server.daemon.equity.histeodupdate;

import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataPartImporter ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.daemon.util.EventRateMonitor ;

public class EquityHistDataImporterDaemon extends Thread {

    private static final Logger log = Logger.getLogger( EquityHistDataImporterDaemon.class ) ;

    public static final String CFG_GRP_NAME = EquityHistDataPartImporter.CFG_GRP_NAME ;
    
    public static final String CFG_PAUSE_REFRESH_FLAG = "pause_import" ;
    public static final String CFG_IMPORT_DELAY_SECS  = "import_delay_secs" ;
    public static final String CFG_PRINT_DEBUG_STMT   = "debug_enable" ;
    
    public static final int MIN_IMPORT_DELAY_SEC = 5 ;

    private EquityHistDataPartImporter partImporter = null ;
    
    private boolean pauseRefresh = true ;
    private int     refreshDelay = MIN_IMPORT_DELAY_SEC ;
    private boolean debugEnable  = false ;
    
    // In the last 300 seconds, if we have 5 exceptions or more, the rate
    // monitor will keep the threshold breached flag till the time the 
    // count goes down below 5
    private EventRateMonitor genericERM = new EventRateMonitor( 300, 5 ) ;

    public EquityHistDataImporterDaemon() {
        partImporter = new EquityHistDataPartImporter() ;
    }
    
    public void run() {
        
        while( true ) {
            try {
                if( !genericERM.hasThresholdBreached() ) {
                    
                    refreshConfiguration() ;
                    
                    if( pauseRefresh ) {
                        if( debugEnable ) {
                            log.debug( "EOD update refresh paused." ) ;
                        }
                    }
                    else {
                        partImporter = new EquityHistDataPartImporter() ;
                        partImporter.execute() ;
                    }
                }
                else {
                    log.info( "Exception rate threshold breached." ) ;
                }
                
                if( debugEnable ) {
                    log.debug( "\nSleeping for " + refreshDelay + " seconds.\n" ) ;
                }
                TimeUnit.SECONDS.sleep( refreshDelay ) ;
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
                    log.error( "EOD update daemon exception stall interrrupted.", ie ) ;
                    break ;
                }
            }
        }
    }
    
    private void refreshConfiguration() {
        
        NVPManager nvpMgr = NVPManager.instance() ;
        NVPConfigGroup cfg = nvpMgr.getConfigGroup( CFG_GRP_NAME ) ; ;

        pauseRefresh = cfg.getBoolValue( CFG_PAUSE_REFRESH_FLAG, pauseRefresh ) ;
        refreshDelay = cfg.getIntValue ( CFG_IMPORT_DELAY_SECS,  refreshDelay ) ;
        debugEnable  = cfg.getBoolValue( CFG_PRINT_DEBUG_STMT,   debugEnable  ) ;
        
        // Hardening - Prevents accidental setting of refresh delay to a lower
        // value which will result in a tight loop and API rate threshold
        // breach.
        if( refreshDelay < MIN_IMPORT_DELAY_SEC ) {
            refreshDelay = MIN_IMPORT_DELAY_SEC ;
            cfg.setValue( CFG_IMPORT_DELAY_SECS, MIN_IMPORT_DELAY_SEC ) ;
        }
    }
}
