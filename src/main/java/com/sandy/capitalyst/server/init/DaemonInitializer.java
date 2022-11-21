package com.sandy.capitalyst.server.init;

import static com.sandy.capitalyst.server.CapitalystServer.CFG_RUN_BATCH_DAEMON ;
import static com.sandy.capitalyst.server.CapitalystServer.CFG_RUN_EQ_HIST_EOD_DAEMON ;
import static com.sandy.capitalyst.server.CapitalystServer.CFG_RUN_EQ_ITD_DAEMON ;
import static com.sandy.capitalyst.server.CapitalystServer.* ;
import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;

import java.io.File ;

import org.apache.log4j.Logger ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.listener.InvStatsPersistListener ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJobScheduler ;
import com.sandy.capitalyst.server.daemon.equity.histeodupdate.EquityHistDataImporterDaemon ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDImporterDaemon ;
import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.PortfolioMarketPriceUpdater ;
import com.sandy.capitalyst.server.daemon.index.histeodupdate.IndexHistDataImporterDaemon ;

@Component
public class DaemonInitializer {

    private static final Logger log = Logger.getLogger( DaemonInitializer.class ) ;
    
    public void initialize() throws Exception {
        
        NVPConfigGroup nvpCfg = NVPManager.instance()
                                          .getConfigGroup( CFG_GRP_APP ) ;

        initializeBatchDaemon( nvpCfg ) ;
        initializeEquityHistEoDUpdateDaemon( nvpCfg ) ;
        initializeIndexHistEoDUpdateDaemon( nvpCfg ) ;
        initializeEquityITDDaemon( nvpCfg ) ;
        runCMPUpdateDaemon( nvpCfg ) ;
    }
    
    private void initializeBatchDaemon( NVPConfigGroup nvpCfg ) 
        throws Exception {
        
        boolean runDaemon = true ;
        
        runDaemon = nvpCfg.getBoolValue( CFG_RUN_BATCH_DAEMON, true ) ;
        
        if( runDaemon ) {
            log.debug( "Initializing scheduler" ) ;
            getBean( CapitalystJobScheduler.class ).initialize() ;
        }
    }

    private void initializeEquityHistEoDUpdateDaemon( NVPConfigGroup nvpCfg ) {
        
        boolean runDaemon = true ;
        EquityHistDataImporterDaemon daemon = null ;
        
        runDaemon = nvpCfg.getBoolValue( CFG_RUN_EQ_HIST_EOD_DAEMON, true ) ;
        
        if( runDaemon ) {
            
            log.debug( "Starting historic Equity eod update daemon." ) ;
            daemon = new EquityHistDataImporterDaemon() ;
            daemon.start() ;
        }
    }
    
    private void initializeIndexHistEoDUpdateDaemon( NVPConfigGroup nvpCfg ) {
        
        boolean runDaemon = true ;
        IndexHistDataImporterDaemon daemon = null ;
        
        runDaemon = nvpCfg.getBoolValue( CFG_RUN_IDX_HIST_EOD_DAEMON, true ) ;
        
        if( runDaemon ) {
            
            log.debug( "Starting historic Index eod update daemon." ) ;
            daemon = new IndexHistDataImporterDaemon() ;
            daemon.start() ;
        }
    }
    
    private void initializeEquityITDDaemon( NVPConfigGroup nvpCfg ) {
        
        boolean runDaemon = true ;
        EquityITDImporterDaemon daemon = null ;
        
        runDaemon = nvpCfg.getBoolValue( CFG_RUN_EQ_ITD_DAEMON, true ) ;
        
        if( runDaemon ) {
            
            log.debug( "Starting equity ITD daemon." ) ;
            daemon = getBean( EquityITDImporterDaemon.class ) ;
            daemon.start() ;
        }
    }
    
    private void runCMPUpdateDaemon( NVPConfigGroup nvpCfg ) 
        throws Exception {
        
        boolean runDaemon = true ;
        File cfgPath = getConfig().getBreezeCfgFile() ;
        
        runDaemon = nvpCfg.getBoolValue( CFG_RUN_CMP_DAEMON, true ) ;
        
        if( runDaemon ) {
            Breeze breeze = Breeze.instance() ;
            breeze.addInvocationListener( new InvStatsPersistListener() ) ;
            breeze.initialize( cfgPath ) ;
            
            log.debug( "Initilizaing Portfolio CMP updater." ) ;
            PortfolioMarketPriceUpdater pmpUpdater = null ;
            pmpUpdater = PortfolioMarketPriceUpdater.instance() ;
            pmpUpdater.initialize() ;
            pmpUpdater.start() ;
            
            log.debug( "  Breeze initialized." ) ;
        }
    }
}
