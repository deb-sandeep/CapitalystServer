package com.sandy.capitalyst.server.api.ledger;

import java.text.SimpleDateFormat ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.ledger.helpers.CCTxnEntry ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.ledger.classifier.LEClassifier ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;

@RestController
public class CCLedgerRestController {

    private static final Logger log = Logger.getLogger( CCLedgerRestController.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    
    @Autowired
    private LedgerRepo ledgerRepo = null ;
    
    @Autowired
    private AccountRepo accountRepo = null ;
    
    private LEClassifier classifier = new LEClassifier() ;
    
    /*
     * Note that the request elements only contain the following attributes. 
     * 
     * 1. valueDate
     * 2. remarks
     * 3. amount
     * 4. balance
     * 5. creditCardNumber
     * 
     * The creditCardNumber is provided so that the account can be looked
     * up at the server end.
     */
    @PostMapping( "/Ledger/CCTxnEntries" )
    public ResponseEntity<APIResponse> importCCTxnEntries( 
                                    @RequestBody List<CCTxnEntry> entries ) {
        try {
            int numSaved = 0 ;
            if( !entries.isEmpty() ) {
                numSaved = processTxnEntries( entries ) ;
            }
            String msg = "Saved " + numSaved + " out of " + entries.size() + " transactions." ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    private int processTxnEntries( List<CCTxnEntry> entries ) 
        throws Exception {
        
        LedgerEntry ledgerEntry = null ;
        LedgerEntry possibleDup = null ;
        int numSaved = 0 ;
        
        updateCCDues( entries.get( 0 ) ) ;
        
        for( CCTxnEntry txnEntry : entries ) {
            ledgerEntry = createLedgerEntry( txnEntry ) ;
            possibleDup = ledgerRepo.findByHash( ledgerEntry.getHash() ) ;
            if( possibleDup == null ) {
                log.debug( "\tSaving ledger entry = " + 
                           SDF.format( ledgerEntry.getValueDate() ) + " - " +
                           ledgerEntry.getRemarks() + " :: " + 
                           ledgerEntry.getAmount() ) ; 
                
                classifier.classifyEntry( ledgerEntry, null ) ;
                ledgerRepo.save( ledgerEntry ) ;
                numSaved++ ;
            }
            else {
                log.info( "Found a duplicate entry : " + txnEntry ) ;
            }
        }
        
        return numSaved ;
    }
    
    private LedgerEntry createLedgerEntry( CCTxnEntry txnEntry ) 
        throws Exception {
        
        LedgerEntry ledgerEntry = null ;
        Account account = accountRepo.findByAccountNumber( txnEntry.getCreditCardNumber() ) ;
        
        ledgerEntry = new LedgerEntry() ;
        ledgerEntry.setAccount( account ) ;
        ledgerEntry.setRemarks( txnEntry.getRemarks() ) ;
        ledgerEntry.setValueDate( txnEntry.getValueDate() ) ;
        ledgerEntry.setAmount( txnEntry.getAmount() ) ;
        
        ledgerEntry.generateHash() ;
        
        ledgerEntry.setBalance( txnEntry.getBalance() ) ;
        return ledgerEntry ;
    }
    
    private void updateCCDues( CCTxnEntry txnEntry ) {
        
        Account account = accountRepo.findByAccountNumber( txnEntry.getCreditCardNumber() ) ;
        account.setBalance( txnEntry.getBalance() ) ;
        accountRepo.save( account ) ;
    }
}
