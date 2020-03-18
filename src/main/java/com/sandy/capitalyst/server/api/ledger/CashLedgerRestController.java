package com.sandy.capitalyst.server.api.ledger;

import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.format.annotation.DateTimeFormat ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.repo.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;
import com.sandy.common.util.StringUtil ;

@RestController
public class CashLedgerRestController {

    private static final Logger log = Logger.getLogger( CashLedgerRestController.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private AccountRepo accountRepo = null ;
    
    @GetMapping( "/Ledger/CashAtHome" )
    public ResponseEntity<List<LedgerEntry>> getCashAtHomeLedgerEntries(
                @RequestParam( name="fromDate" ) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Date fromDate,
                
                @RequestParam( name="toDate" ) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Date toDate ) {
        try {
            Account account = accountRepo.findByAccountNumber( "CASH@HOME" ) ;
            
            List<LedgerEntry> entries = null ;
            
            entries = lRepo.findEntries( account.getId(), fromDate, toDate ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Ledger/CashAtHome" ) 
    public ResponseEntity<LedgerEntry> saveCashEntry( 
                                     @RequestBody LedgerEntry ledgerEntry ) {
        try {
            Account account = accountRepo.findByAccountNumber( "CASH@HOME" ) ;
            ledgerEntry.setAccount( account ) ;
            ledgerEntry.setAmount( -1*ledgerEntry.getAmount() ) ;
            
            String hash = generateCashEntryHash( ledgerEntry ) ;
            
            if( ledgerEntry.getId() == null || 
                ledgerEntry.getId() == -1 ) {
                LedgerEntry dup = lRepo.findByHash( hash ) ;
                if( dup != null ) {
                    return ResponseEntity.status( HttpStatus.CONFLICT )
                                         .body( null ) ;
                }
            }
            
            ledgerEntry.setHash( hash ) ;
            ledgerEntry = lRepo.save( ledgerEntry ) ;
            
            Float balance = lRepo.computeCashAccountBalance( account.getId() ) ;
            ledgerEntry.setBalance( balance ) ;
            account.setBalance( balance ) ;

            ledgerEntry = lRepo.save( ledgerEntry ) ;
            accountRepo.save( account ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( ledgerEntry ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    private String generateCashEntryHash( LedgerEntry entry ) throws Exception {
        
        StringBuffer buffer = new StringBuffer() ;
        buffer.append( LedgerEntry.HASH_SDF.format( entry.getValueDate() ) )
              .append( entry.getL1Cat() )
              .append( entry.getL2Cat() )
              .append( entry.getAmount() ) ;
        return StringUtil.getHash( buffer.toString() ) ;
    }
}
