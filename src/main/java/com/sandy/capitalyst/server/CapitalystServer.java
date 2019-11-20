package com.sandy.capitalyst.server ;

import java.io.File ;

import org.apache.commons.io.FileUtils ;
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
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRule ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifierRuleBuilder ;
import com.sandy.capitalyst.server.core.ledger.loader.LedgerImporter ;
import com.sandy.capitalyst.server.core.ledger.loader.LedgerImporterFactory ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountIndexRepo ;
import com.sandy.capitalyst.server.dao.ledger.AccountLedgerRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.common.bus.EventBus ;

@SpringBootApplication
public class CapitalystServer 
    implements ApplicationContextAware, WebMvcConfigurer {

    private static final Logger log = Logger.getLogger( CapitalystServer.class ) ;
    
    private static ApplicationContext APP_CTX   = null ;
    private static CapitalystServer   APP       = null ;
    
    public static EventBus GLOBAL_EVENT_BUS = new EventBus() ;
    
    public static ApplicationContext getAppContext() {
        return APP_CTX ;
    }

    public static CapitalystConfig getConfig() {
        return (CapitalystConfig) APP_CTX.getBean( "config" ) ;
    }

    public static CapitalystServer getApp() {
        return APP ;
    }

    // ---------------- Instance methods start ---------------------------------

    @Autowired
    private AccountIndexRepo accountIndexRepo = null ;
    
    @Autowired
    private AccountLedgerRepo alRepo = null ;
    
    public CapitalystServer() {
        APP = this ;
    }

    public void importLedgerEntries() throws Exception {
        Account account = accountIndexRepo.findByAccountNumber( "000501005212" ) ;
        LedgerImporter li = LedgerImporterFactory.getLedgerImporter( account ) ;
        
        File dir = new File( "/home/sandeep/Downloads" ) ;
        for( int year = 2011; year<=2019; year++ ) {
            File file = new File( dir, "Stmt-" + year + ".xls" ) ;
            li.importLedgerEntries( account, file ) ;
        }
    }
    
    public void testLEClassifier() throws Exception {
        File file = new File( "src/test/resources/rule.txt" ) ;
        String ruleText = FileUtils.readFileToString( file ) ;
        LEClassifierRule rule = new LEClassifierRuleBuilder().buildClassifier( ruleText ) ;
        for( LedgerEntry entry : alRepo.findAll() ) {
            if( rule.isRuleMatched( entry ) ) {
                log.debug( entry.getRemarks() ) ;
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
//        CapitalystServer app = CapitalystServer.getAppContext().getBean( CapitalystServer.class ) ;
//        app.importLedgerEntries() ;
//        app.testLEClassifier() ;
    }
}
