package com.sandy.capitalyst.server.core.ledger.loader;

import java.io.File ;

import org.springframework.context.ApplicationContext ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountIndexRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;

public abstract class LedgerImporter {
    
    protected LedgerRepo ledgerRepo = null ;
    protected AccountIndexRepo accountIndexRepo = null ;
    
    protected LedgerImporter() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        this.ledgerRepo = appCtx.getBean( LedgerRepo.class ) ;
        this.accountIndexRepo = appCtx.getBean( AccountIndexRepo.class ) ;
    }

    public abstract LedgerImportResult importLedgerEntries( Account account, File file )
        throws Exception ;
}
