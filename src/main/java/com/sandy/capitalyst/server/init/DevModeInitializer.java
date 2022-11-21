package com.sandy.capitalyst.server.init;

import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;

import org.apache.log4j.Logger ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfig ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDImporterDaemon ;
import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.PortfolioMarketPriceUpdater ;

@Component
public class DevModeInitializer {

    static final Logger log = Logger.getLogger( DevModeInitializer.class ) ;
    
    private NVPManager nvpMgr = null ;
    
    public void initialize() throws Exception {
        
        if( !getConfig().isDevMode() ) return ;
        
        this.nvpMgr = NVPManager.instance() ;
        
        forceOn( EquityITDImporterDaemon.CFG_GRP_NAME, 
                 EquityITDImporterDaemon.CFG_PAUSE_REFRESH_FLAG,
                 "Equity ITD Import Daemon" ) ;

        forceOn( PortfolioMarketPriceUpdater.CFG_GRP_NAME, 
                 PortfolioMarketPriceUpdater.CFG_PAUSE_REFRESH_FLAG,
                 "Breeze Portfolio CMP Daemon (pause)" ) ;
        
        forceOff( CapitalystServer.CFG_GRP_APP, 
                  CapitalystServer.CFG_RUN_BATCH_DAEMON, 
                  "Batch Scheduler Daemon" ) ;
        
        forceOff( CapitalystServer.CFG_GRP_APP, 
                  CapitalystServer.CFG_RUN_EQ_HIST_EOD_DAEMON, 
                  "Bhavcopy Import Daemon (start)" ) ;
        
        forceOff( CapitalystServer.CFG_GRP_APP, 
                  CapitalystServer.CFG_RUN_IDX_HIST_EOD_DAEMON, 
                  "Index EOD Daemon (start)" ) ;
        
        forceOff( CapitalystServer.CFG_GRP_APP, 
                  CapitalystServer.CFG_RUN_EQ_ITD_DAEMON, 
                  "Equity ITD Daemon (start)" ) ;
        
        forceOff( CapitalystServer.CFG_GRP_APP, 
                  CapitalystServer.CFG_RUN_CMP_DAEMON, 
                  "Breeze Portfolio CMP Start Daemon (start)" ) ;
        
    }
    
    private void forceOn( String cfgGrp, String cfgKey, String desc ) {
        
        log.debug( "DevMode :: Force ON " + desc ) ;
        NVPConfig cfg = nvpMgr.getConfig( cfgGrp, cfgKey, "false" ) ;
        cfg.setValue( true ) ;
    }

    private void forceOff( String cfgGrp, String cfgKey, String desc ) {
        
        log.debug( "DevMode :: Force OFF " + desc ) ;
        NVPConfig cfg = nvpMgr.getConfig( cfgGrp, cfgKey, "false" ) ;
        cfg.setValue( false ) ;
    }
}
