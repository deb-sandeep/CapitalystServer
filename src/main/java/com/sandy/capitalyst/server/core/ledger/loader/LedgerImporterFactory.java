package com.sandy.capitalyst.server.core.ledger.loader;

import com.sandy.capitalyst.server.dao.account.Account ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.* ;

import org.apache.log4j.Logger ;

public class LedgerImporterFactory {
    
    private static Logger log = Logger.getLogger( LedgerImporterFactory.class ) ;

    public static LedgerImporter getLedgerImporter( Account account ) {
        if( account.getBankName().equals( Bank.ICICI.name() ) &&
            account.getAccountType().equals( AccountType.SAVING.name() ) ) {
            return new ICICISavingsAccountLedgerImporter() ;
        }
        
        log.error( "LedgerImporter not defined for account = " + account ) ;
        return null ;
    }
}
