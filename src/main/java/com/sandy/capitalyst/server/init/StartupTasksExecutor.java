package com.sandy.capitalyst.server.init;

import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;

import java.io.File ;
import java.net.URL;
import java.util.Objects;

import com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDSnapshotService;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.listener.InvStatsPersistListener ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.CapitalystConstants.Bank ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@Component
public class StartupTasksExecutor {

    private static final Logger log = Logger.getLogger( StartupTasksExecutor.class ) ;
    
    private AccountRepo aiRepo = null ;
    private LedgerRepo ledgerRepo = null ;
    private EquityLTPRepository ltpRepository ;
    private EquityITDSnapshotService itdSnapshotService ;

    @Autowired
    public void setAccountRepo( AccountRepo repo ) {
        this.aiRepo = repo ;
    }

    @Autowired
    public void setLedgerRepo( LedgerRepo repo ) {
        this.ledgerRepo = repo ;
    }

    @Autowired
    public void setEquityLTPRepository( EquityLTPRepository repo ) {
        this.ltpRepository = repo ;
    }

    @Autowired
    public void setEquityITDSnapshotService( EquityITDSnapshotService svc ) {
        this.itdSnapshotService = svc ;
    }

    public void initialize() throws Exception {
        
        CapitalystConfig cfg = CapitalystServer.getConfig() ;

        assert cfg != null;
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

        this.ltpRepository.init() ;
        this.itdSnapshotService.init() ;
        
        log.debug( "Startup Task :: Initializing Breeze subsystem" ) ; 
        initializeBreeze() ;
    }

    private void initializeRecoManager() {
        
        Thread t = new Thread(() -> {
            try {
                Thread.sleep( 1000 ) ;
                RecoManager.instance().getAllRecos() ;
                log.debug( "  Recommendation manager initialized" ) ;
            }
            catch( Exception e ) {
                log.error( "  Reco manager initialization failed.", e ) ;
            }
        });
        t.start() ;
    }
    
    private void updateAccountBalanceOnStartup() {
        
        for( Account account : aiRepo.findAll() ) {
            if( account.getBankName().equals( Bank.PO.name() ) ) {
                // Note that PO balance is zero based on the statements 
                // The PO balance is updated when a statements are uploaded.
                // No need to update the balance on startup.
                continue ;
            }
            Float balance = this.ledgerRepo.getAccountBalance( account.getId() ) ;
            if( balance != null ) {
                account.setBalance( balance ) ;
                this.aiRepo.save( account ) ;
            }
        }
    }
    
    private void initializeBreeze() throws Exception {
        
        File cfgPath = Objects.requireNonNull(getConfig()).getBreezeCfgFile() ;
        if( cfgPath == null || !cfgPath.exists() ) {
            log.info( "Breeze configuration not found. Trying classpath." ) ;
            URL url = getClass().getResource( "/breeze-config.yaml" ) ;
            if( url != null ) {
                cfgPath = new File( url.toURI().getPath() ) ;
            }
            else {
                log.error( "Breeze configuration not found." ) ;
                throw new IllegalStateException( "Breeze configuration not found" ) ;
            }
        }
        Breeze breeze = Breeze.instance() ;
        breeze.addInvocationListener( new InvStatsPersistListener() ) ;
        breeze.initialize( cfgPath ) ;
    }
}
