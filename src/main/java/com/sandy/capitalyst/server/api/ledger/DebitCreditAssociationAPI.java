package com.sandy.capitalyst.server.api.ledger;

import static org.springframework.http.ResponseEntity.status ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.dao.DataIntegrityViolationException ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.repo.DebitCreditAssocRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

@RestController
public class DebitCreditAssociationAPI {

    private static final Logger log = Logger.getLogger( DebitCreditAssociationAPI.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private DebitCreditAssocRepo dcaRepo = null ;
    
    @GetMapping( "/DebitCreditAssociation/{ledgerEntryId}" ) 
    public ResponseEntity<List<DebitCreditAssoc>> get( 
                                         @PathVariable Integer ledgerEntryId ) {
        try {
            List<DebitCreditAssoc> associations = null ;
            LedgerEntry le = lRepo.findById( ledgerEntryId ).get() ;
            
            if( le.isCredit() ) {
                associations = dcaRepo.findByCreditTxnId( ledgerEntryId ) ;
            }
            else {
                associations = dcaRepo.findByDebitTxnId( ledgerEntryId ) ;
            }
            
            return status( HttpStatus.OK ).body( associations ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting association entries.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    @GetMapping( "/DebitCreditAssociation/AssociatedIds" ) 
    public ResponseEntity<List<Integer>> get() {
        try {
            List<Integer> associatedIds = new ArrayList<>() ;
            
            associatedIds.addAll( dcaRepo.findDistinctDebitTxnId() ) ;
            associatedIds.addAll( dcaRepo.findDistinctCreditTxnId() ) ;
            
            return status( HttpStatus.OK ).body( associatedIds ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting association entries.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    @PostMapping( "/DebitCreditAssociation" ) 
    public ResponseEntity<APIResponse> save( 
                         @RequestBody List<DebitCreditAssoc> associations ) {
        try {
            dcaRepo.saveAll( associations ) ;
            return status( HttpStatus.OK ).body( APIResponse.SUCCESS ) ;
        }
        catch( DataIntegrityViolationException d ) {
            log.error( "Error: Duplicate entry. " + d.getMessage() ) ;
            return status( HttpStatus.BAD_REQUEST )
                   .body( new APIResponse( "Duplicate credit debit association." ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving association entry.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @DeleteMapping( "/DebitCreditAssociation/{id}" ) 
    public ResponseEntity<APIResponse> delete( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting association entry. " + id ) ;
            dcaRepo.deleteById( id ) ;
            
            return status( HttpStatus.OK )
                   .body( new APIResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Deleting association entry.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
