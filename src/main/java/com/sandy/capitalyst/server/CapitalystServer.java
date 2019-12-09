package com.sandy.capitalyst.server ;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.List ;

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
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.common.bus.EventBus ;
import com.sandy.common.xlsutil.XLSRow ;
import com.sandy.common.xlsutil.XLSWrapper ;

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

    public CapitalystServer() {
        APP = this ;
    }
    
    public void initialize() throws Exception {
        if( CapitalystServer.getConfig().isRunClassificationOnStartup() ) {
            LEClassifier classifier = new LEClassifier() ;
            classifier.runClassification() ;
        }
        updateAccountBalanceOnStartup() ;
    }
    
    private void updateAccountBalanceOnStartup() {
        
        for( Account account : aiRepo.findAll() ) {
            log.debug( "Updating balance for account = " + account.getShortName() ) ;
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
    
    public void importHistoricCCEntries() throws Exception {
        
        SimpleDateFormat DF = new SimpleDateFormat( "dd/MM/yy" ) ;
        XLSWrapper wrapper = new XLSWrapper( new File( "/Users/sandeep/temp/CCLog.xls" ) ) ;
        List<XLSRow> rows = wrapper.getRows( 0, 0, 5 ) ;
        Account account = aiRepo.findById( 7671 ).get() ;
        
        int numRowsImported = 0 ;
        
        for( XLSRow row : rows ) {
            LedgerEntry le = new LedgerEntry() ;
            le.setAccount( account ) ;
            le.setAmount( -1 * Float.parseFloat( row.getCellValue( 3 ) ) ) ;
            le.setBalance( Float.parseFloat( row.getCellValue( 5 ) ) ) ;
            le.setNotes( "CC X" + row.getCellValue( 0 ) ) ;
            le.setRemarks( row.getCellValue( 2 ) ) ;
            le.setValueDate( DF.parse( row.getCellValue( 1 ) ) ) ;
            le.generateHash() ;
            
            ledgerRepo.save( le ) ;
            
            account.setBalance( le.getBalance() ) ;
            aiRepo.save( account ) ;
            
            log.debug( "Num rows imported = " + ++numRowsImported ) ;
        }
    }
}
