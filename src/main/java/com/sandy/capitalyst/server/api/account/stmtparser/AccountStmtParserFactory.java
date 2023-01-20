package com.sandy.capitalyst.server.api.account.stmtparser;

import static com.sandy.capitalyst.server.core.CapitalystConstants.AccountType.CREDIT ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.AccountType.SAVING ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.Bank.ICICI ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.Bank.SBI ;
import static com.sandy.capitalyst.server.core.CapitalystConstants.Bank.PO ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.account.Account ;

public class AccountStmtParserFactory {
    
    private static Logger log = Logger.getLogger( AccountStmtParserFactory.class ) ;

    public static AccountStmtParser getParser( Account account ) {
        
        AccountStmtParser importer = null ;
        String accountType = account.getAccountType() ;
        
        if( accountType.equals( SAVING.name() ) ) {
            importer = getSBStmtParser( account ) ;
        }
        else if( accountType.equals( CREDIT.name() ) ) {
            importer = new ICICICreditCardAccountStmtParser() ;
        }

        if( importer == null ) {
            log.error( "LedgerImporter not defined for account = " + account ) ;
        }
        
        return importer ;
    }
    
    private static AccountStmtParser getSBStmtParser( Account account ) {
        
        AccountStmtParser importer = null ;
        String bankName = account.getBankName() ;
        
        if( bankName.equals( ICICI.name() ) ) {
            importer = new ICICISavingsAccountStmtParser() ;
        }
        else if( bankName.equals( SBI.name() ) ) {
            importer = new SBISavingsAccountStmtParser() ;
        }
        else if( bankName.equals( PO.name() ) ) {
            importer = new POSavingsAccountStmtParser() ;
        }
        
        if( importer == null ) {
            log.error( "LedgerImporter not defined for bank = " + bankName ) ;
        }
        
        return importer ;
    }
}
