package com.sandy.capitalyst.server.core.ledger.importer;

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

public class LedgerImporter {
    
    private static final Logger log = Logger.getLogger( LedgerImporter.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    
    protected LedgerRepo ledgerRepo = null ;
    protected AccountRepo accountRepo = null ;
    
    public LedgerImporter() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        if( appCtx != null ) {
            this.ledgerRepo = appCtx.getBean( LedgerRepo.class ) ;
            this.accountRepo = appCtx.getBean( AccountRepo.class ) ;
        }
    }

    // NOTE: It is assumed that all the ledger entries are for a single 
    //       account. Implying, 
    public LedgerImportResult importLedgerEntries( List<LedgerEntry> entries )
        throws Exception {
        
        Account acc = null ;
        LEClassifier classifier = new LEClassifier() ;
        LedgerEntry possibleDup = null ;
        LedgerImportResult result = new LedgerImportResult() ;
        
        if( entries.isEmpty() ) {
            result.getErrMsgs().add( "No ledger entries specified." ) ;
            return result ;
        }
        
        acc = entries.get(0).getAccount() ;
        
        log.debug( "Importing ledger entries for A/C " + acc.getShortName() ) ;
        log.debug( "\tNum ledger entries = " + entries.size() ) ;
        
        result.setNumEntriesFound( entries.size() ) ;
        
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
                log.debug( "Found a duplicate entry " + entry ) ;
                result.incrementDupCount() ;
            }
        }
        
        // Update the account balance by taking the latest balance from ledger
        updateAccountBalance( acc ) ;
        
        log.debug( "\tNum entries saved = " + result.getNumEntriesImported() ) ;
        log.debug( "\tNum duplicate entries = " + result.getNumDuplicateEntries() ) ;
        
        return result ;
    }
    
    public void updateAccountBalance( Account account ) {
        
        Float balance = this.ledgerRepo.getAccountBalance( account.getId() ) ;
        if( balance != null ) {
            account.setBalance( balance ) ;
            this.accountRepo.save( account ) ;
        }
    }
}
