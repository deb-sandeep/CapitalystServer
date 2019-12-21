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

import com.sandy.capitalyst.server.config.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJobScheduler ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
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
    
    public void initialize() throws Exception {
        
        if( CapitalystServer.getConfig().isRunClassificationOnStartup() ) {
            LEClassifier classifier = new LEClassifier() ;
            classifier.runClassification() ;
        }
        log.debug( "Updating account balances" ) ;
        updateAccountBalanceOnStartup() ;

        log.debug( "Initializing scheduler" ) ;
        scheduler.initialize() ;
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
