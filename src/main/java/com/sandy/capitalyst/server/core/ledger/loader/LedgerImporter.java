package com.sandy.capitalyst.server.core.ledger.loader;

import java.io.File ;

import org.springframework.context.ApplicationContext ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountIndexRepo ;
import com.sandy.capitalyst.server.dao.ledger.AccountLedgerRepo ;

public abstract class LedgerImporter {
    
    protected AccountLedgerRepo ledgerRepo = null ;
    protected AccountIndexRepo accountIndexRepo = null ;
    
    protected LedgerImporter() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        this.ledgerRepo = appCtx.getBean( AccountLedgerRepo.class ) ;
        this.accountIndexRepo = appCtx.getBean( AccountIndexRepo.class ) ;
    }

    public abstract void importLedgerEntries( Account account, File file )
        throws Exception ;
}
