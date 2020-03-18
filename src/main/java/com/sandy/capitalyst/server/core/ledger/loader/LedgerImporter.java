package com.sandy.capitalyst.server.core.ledger.loader;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.context.ApplicationContext ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public abstract class LedgerImporter {
    
    private static final Logger log = Logger.getLogger( LedgerImporter.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    
    private LEClassifier classifier = new LEClassifier() ;
    
    protected LedgerRepo ledgerRepo = null ;
    protected AccountRepo accountIndexRepo = null ;
    
    protected LedgerImporter() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        if( appCtx != null ) {
            this.ledgerRepo = appCtx.getBean( LedgerRepo.class ) ;
            this.accountIndexRepo = appCtx.getBean( AccountRepo.class ) ;
        }
    }

    public final LedgerImportResult importLedgerEntries( Account account, File file )
        throws Exception {
        
        LedgerImportResult result = new LedgerImportResult() ;
        result.setFileName( file.getName() ) ;
        
        log.debug( "Parsing ledger entries for account " + account.getShortName() ) ;
        
        List<LedgerEntry> entries = parseLedgerEntries( account, file ) ;
        log.debug( "Ledger entries parsed." ) ;
        log.debug( "\tNum ledger entries = " + entries.size() ) ;
        result.setNumEntriesFound( entries.size() ) ;
        
        LedgerEntry possibleDup = null ;
        for( LedgerEntry entry : entries ) {
            possibleDup = ledgerRepo.findByHash( entry.getHash() ) ;
            if( possibleDup == null ) {
                log.debug( "\tSaving ledger entry = " + 
                           SDF.format( entry.getValueDate() ) + " - " +
                           entry.getRemarks() + " :: " + 
                           entry.getAmount() ) ; 
                
                classifier.classifyEntry( entry, null ) ;
                ledgerRepo.save( entry ) ;
                result.incrementImportCount() ;
            }
            else {
                log.info( "Found a duplicate entry " + entry ) ;
                result.incrementDupCount() ;
            }
        }
        log.debug( "Ledger entries saved" ) ;
        return result ;
    }
    
    protected abstract List<LedgerEntry> parseLedgerEntries( 
                                                Account account, File file )
        throws Exception ;
    
    public void updateAccountBalance( Account account ) {
        Float balance = this.ledgerRepo.getAccountBalance( account.getId() ) ;
        if( balance != null ) {
            account.setBalance( balance ) ;
            this.accountIndexRepo.save( account ) ;
        }
    }
}
