package com.sandy.capitalyst.server ;

import java.io.File ;

import org.apache.log4j.Logger ;
import org.springframework.beans.BeansException ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.boot.SpringApplication ;
import org.springframework.boot.autoconfigure.SpringBootApplication ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.ApplicationContextAware ;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry ;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.listener.InvStatsPersistListener ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJobScheduler ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;
import com.sandy.common.bus.EventBus ;

@SpringBootApplication
public class CapitalystServer 
    implements ApplicationContextAware, WebMvcConfigurer {

    static final Logger log = Logger.getLogger( CapitalystServer.class ) ;
    
    private static ApplicationContext APP_CTX   = null ;
    private static CapitalystServer   APP       = null ;
    
    public static EventBus GLOBAL_EVENT_BUS = new EventBus() ;
    
    public static String CFG_GRP_APP = "Capitalyst" ;
    
    public static String CFG_RUN_CMP_DAEMON   = "run_portfolio_cmp_updater" ;
    public static String CFG_RUN_BATCH_DAEMON = "run_batch_daemon" ;
    
    public static ApplicationContext getAppContext() {
        return APP_CTX ;
    }

    public static CapitalystConfig getConfig() {
        if( APP_CTX == null ) return null ;
        return (CapitalystConfig) APP_CTX.getBean( "config" ) ;
    }
    
    public static <T> T getBean( Class<T> type ) {
        return APP_CTX.getBean( type ) ;
    }

    public static CapitalystServer getApp() {
        return APP ;
    }
    
    public static boolean isInServerMode() {
        return APP != null ;
    }

    // ---------------- Instance methods start ------------------------------
    @Autowired
    private AccountRepo aiRepo = null ;
    
    @Autowired
    private LedgerRepo ledgerRepo = null ;
    
    @Autowired
    private CapitalystJobScheduler scheduler = null ;
    
    private NVPManager nvpManger = null ;

    public CapitalystServer() {
        APP = this ;
    }
    
    public CapitalystJobScheduler getScheduler() {
        return this.scheduler ;
    }
    
    public void initialize() throws Exception {
        
        this.nvpManger = NVPManager.instance() ;

        CapitalystConfig cfg = CapitalystServer.getConfig() ;
        NVPConfigGroup   nvpCfg = nvpManger.getConfigGroup( CFG_GRP_APP ) ;
        
        if( cfg.isRunClassificationOnStartup() ) {
            
            log.debug( "Running Ledger Classifier" ) ;
            LEClassifier classifier = new LEClassifier() ;
            classifier.runClassification() ;
        }
        else {
            log.debug( "Skipping Ledger Classifier" ) ;
        }
        
        log.debug( "Updating account balances" ) ;
        updateAccountBalanceOnStartup() ;
        
        if( cfg.isInitializeRecoMgrOnStart() ) {
            log.debug( "Initilizaing recommendation manager." ) ;
            initializeRecoManager() ;
        }
        
        boolean startBatchDaemon = true ;
        startBatchDaemon = nvpCfg.getBoolValue( CFG_RUN_BATCH_DAEMON, true ) ;
        if( startBatchDaemon ) {
            log.debug( "Initializing scheduler" ) ;
            scheduler.initialize() ;
        }
        else {
            log.debug( "Skipping scheduler initialization" ) ;
        }
        
        boolean runCMPUpdateDaemon = true ;
        runCMPUpdateDaemon = nvpCfg.getBoolValue( CFG_RUN_CMP_DAEMON, true ) ;
        initializeBreeze( cfg.getBreezeCfgFile(), runCMPUpdateDaemon ) ;
    }
    
    private void initializeBreeze( File cfgPath, boolean runDaemon ) 
            throws Exception {
        
        InvStatsPersistListener statPersist = new InvStatsPersistListener() ;
        
        Breeze breeze = Breeze.instance() ;
        breeze.addInvocationListener( statPersist ) ;
        breeze.initialize( cfgPath ) ;
        
        /*
        if( runDaemon ) {
            log.debug( "Initilizaing Portfolio CMP updater." ) ;
            PortfolioMarketPriceUpdater pmpUpdater = null ;
            pmpUpdater = PortfolioMarketPriceUpdater.instance() ;
            pmpUpdater.initialize() ;
            pmpUpdater.start() ;
        }*/
        
        log.debug( "  Breeze initialized." ) ;
    }
    
    private void initializeRecoManager() {
        
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep( 1000 ) ;
                    RecoManager.instance().getAllRecos() ;
                    log.debug( "Recommendation manager initialized" ) ;
                }
                catch( Exception e ) {
                    log.error( "Reco manager initialization failed.", e ) ;
                }
            }
        } ;
        t.start() ;
    }
    
    private void updateAccountBalanceOnStartup() {
        for( Account account : aiRepo.findAll() ) {
            Float balance = this.ledgerRepo.getAccountBalance( account.getId() ) ;
            if( balance != null ) {
                account.setBalance( balance ) ;
                this.aiRepo.save( account ) ;
            }
        }
    }
    
    @Override
    public void setApplicationContext( ApplicationContext applicationContext )
            throws BeansException {
        APP_CTX = applicationContext ;
    }

    @Override
    public void addResourceHandlers( ResourceHandlerRegistry registry ) {
    }
    
    // --------------------- Main method ---------------------------------------

    public static void main( String[] args ) throws Exception {
        
        long startTime = System.currentTimeMillis() ;
        log.debug( "Starting Spring Booot..." ) ;
        SpringApplication.run( CapitalystServer.class, args ) ;

        log.debug( "Starting Capitalyst Server.." ) ;
        CapitalystServer app = CapitalystServer.getAppContext()
                                               .getBean( CapitalystServer.class ) ;
        app.initialize() ;
        long endTime = System.currentTimeMillis() ;
        
        int timeTaken = (int)(( endTime - startTime )/1000) ;
        
        log.debug( "" ) ;
        log.debug( "Capitalyst Server open for business. "  + 
                   "Boot time = " + timeTaken + " secs." ) ;
        log.debug( "" ) ;
    }
}
