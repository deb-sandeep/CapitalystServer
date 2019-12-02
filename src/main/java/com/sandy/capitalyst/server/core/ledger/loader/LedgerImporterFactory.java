package com.sandy.capitalyst.server.core.ledger.loader;

import com.sandy.capitalyst.server.dao.account.Account ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.* ;

import org.apache.log4j.Logger ;

public class LedgerImporterFactory {
    
    private static Logger log = Logger.getLogger( LedgerImporterFactory.class ) ;

    public static LedgerImporter getLedgerImporter( Account account ) {
        
        LedgerImporter importer = null ;
        if( account.getAccountType().equals( AccountType.SAVING.name() ) ) {
            importer = getSavingBankLedgerImporter( account ) ;
        }

        if( importer == null ) {
            log.error( "LedgerImporter not defined for account = " + account ) ;
        }
        
        return importer ;
    }
    
    private static LedgerImporter getSavingBankLedgerImporter( Account account ) {
        
        LedgerImporter importer = null ;
        String bankName = account.getBankName() ;
        
        if( bankName.equals( Bank.ICICI.name() ) ) {
            importer = new ICICISavingsAccountLedgerImporter() ;
        }
        else if( bankName.equals( Bank.SBI.name() ) ) {
            importer = new SBISavingsAccountLedgerImporter() ;
        }
        
        return importer ;
    }
}
