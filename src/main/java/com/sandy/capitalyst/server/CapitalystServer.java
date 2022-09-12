package com.sandy.capitalyst.server ;

import org.apache.log4j.Logger ;
import org.springframework.beans.BeansException ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.boot.SpringApplication ;
import org.springframework.boot.autoconfigure.SpringBootApplication ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.ApplicationContextAware ;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry ;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer ;

import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
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

    // ---------------- Instance methods start ------------------------------
    @Autowired
    private AccountRepo aiRepo = null ;
    
    @Autowired
    private LedgerRepo ledgerRepo = null ;
    
    @Autowired
    private CapitalystJobScheduler scheduler = null ;

    public CapitalystServer() {
        APP = this ;
    }
    
    public CapitalystJobScheduler getScheduler() {
        return this.scheduler ;
    }
    
    public void initialize() throws Exception {
        
        CapitalystConfig cfg = CapitalystServer.getConfig() ;
        
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

        if( cfg.isBatchDaemonEnabled() ) {
            log.debug( "Initializing scheduler" ) ;
            scheduler.initialize() ;
        }
        else {
            log.debug( "Skipping scheduler initialization" ) ;
        }
        
        if( cfg.isInitializeRecoMgrOnStart() ) {
            log.debug( "Initilizaing recommendation manager." ) ;
            initializeRecoManager() ;
        }
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
        log.debug( "Starting Spring Booot..." ) ;
        SpringApplication.run( CapitalystServer.class, args ) ;

        log.debug( "Starting Capitalyst Server.." ) ;
        CapitalystServer app = CapitalystServer.getAppContext().getBean( CapitalystServer.class ) ;
        app.initialize() ;
    }
}
