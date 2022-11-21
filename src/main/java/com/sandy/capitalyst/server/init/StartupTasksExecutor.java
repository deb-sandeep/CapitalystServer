package com.sandy.capitalyst.server.init;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@Component
public class StartupTasksExecutor {

    private static final Logger log = Logger.getLogger( StartupTasksExecutor.class ) ;
    
    @Autowired
    private AccountRepo aiRepo = null ;
    
    @Autowired
    private LedgerRepo ledgerRepo = null ;
    
    public void initialize() throws Exception {
        
        CapitalystConfig cfg = CapitalystServer.getConfig() ;
        
        if( cfg.isRunClassificationOnStartup() ) {
            log.debug( "Startup Task :: Running Ledger Classifier" ) ;
            LEClassifier classifier = new LEClassifier() ;
            classifier.runClassification() ;
        }
        else {
            log.debug( "Startup Task :: DevMode :: Not running Ledger Classifier" ) ;
        }
        
        if( cfg.isUpdateAccountBalanceOnStartup() ) {
            log.debug( "Startup Task :: Updating account balances" ) ;
            updateAccountBalanceOnStartup() ;
        }
        else {
            log.debug( "Startup Task :: DevMode :: Not updating account balances" ) ;
        }
        
        if( cfg.isInitializeRecoMgrOnStartup() ) {
            log.debug( "Startup Task :: Initilizaing recommendation manager." ) ;
            initializeRecoManager() ;
        }
        else {
            log.debug( "Startup Task :: DevMode :: Not initializing reco mgr." ) ;
        }
    }

    private void initializeRecoManager() {
        
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep( 1000 ) ;
                    RecoManager.instance().getAllRecos() ;
                    log.debug( "  Recommendation manager initialized" ) ;
                }
                catch( Exception e ) {
                    log.error( "  Reco manager initialization failed.", e ) ;
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
}
