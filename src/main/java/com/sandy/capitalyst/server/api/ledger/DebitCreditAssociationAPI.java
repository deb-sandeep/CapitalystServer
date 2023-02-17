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

import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.repo.DebitCreditAssocRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

/**
 * This function, given a credit transaction entry returns a list of 
 * debit associations whose cost has been fully or partially recovered
 * by this credit entry.
 * 
 * For example, consider the following 
 * 
 * [[ Ledger ]]
 * 
 *                Credit Amt    Debit Amount
 * -----------------------------------------------------------------------------
 * Credit Txn1          1000                  ..... (A)
 * Credit Txn2           500                  ..... (B)
 * Debit  TxnA                         -200   ..... (C)
 * Debit  TxnB                         -100   ..... (D)
 * Debit  TxnC                         -400   ..... (E)
 * 
 * [[ DebitCreditAssociations ]]
 * 
 * Credit Txn        Debit Txn      Recovered 
 *                                     Amount
 * -----------------------------------------------------------------------------
 * Credit Txn1       Debit TxnA          100  ..... (F)
 * Credit Txn1       Debit TxnB           50  ..... (G)
 * Credit Txn2       Debit TxnA           50  ..... (H)
 * Credit Txn2       Debit TxnB           50  ..... (I)
 * Credit Txn3       Debit TxnC          400  ..... (J)
 * 
 * [[ Analysis of credit txn1 associations ]]
 * 
 * Credit Txn1
 *      - Credit Amount            :    1000  ..... (A)
 *      - Remaining amount         :     850  ..... (A-2-3) 
 *      - Debit recoveries
 *          - Debit TxnA           :     100  ..... (2)
 *              - Total amount     :    -200  ..... (C)
 *              - Max recovery amt :     150  ..... (C-3) *
 *              - Recovery credits
 *                  - Credit Txn1  :     100  ..... (F)
 *                  - Credit Txn2  :      50  ..... (H)
 *              - Balance debit    :     -50  ..... (C+F+H)
 *              
 *          - Debit TxnB           :      50  ..... (3)
 *              - Total amount     :    -100  ..... (D)
 *              - Recovery credits
 *                  - Credit Txn1  :      50  ..... (G)
 *                  - Credit Txn2  :      50  ..... (I)
 *              - Balance debit    :       0  ..... (D+G+I)
 * 
 * [[ Analysis of debit txnA associations ]]
 * 
 * Debit TxnA
 *      - Debit Amount             :    -200  ..... (C)
 *      - Credit recoveries
 *          - Credit Txn1          :     100  ..... (F)
 *          - Credit Txn2          :      50  ..... (H)
 *          
 * * - This is an important point. 
 *      While associating a debit entry with a credit for recovery, there is 
 *      a maximum amount of debit can be recovered. This amount apart from
 *      how much of credit is available, also depends upon how much of the 
 *      debit has already been recovered.
 *      
 *      In the above example, for debit txnA, max 150 can be recovered from
 *      Credit Txn1 because 50 has already been recovered by Txn2 (H)
 * 
 */
@RestController
public class DebitCreditAssociationAPI {

    private static final Logger log = Logger.getLogger( DebitCreditAssociationAPI.class ) ;
    
    @Autowired
    private LedgerRepo lRepo = null ;
    
    @Autowired
    private DebitCreditAssocRepo dcaRepo = null ;
    
    @GetMapping( "/DebitAssociation/{creditTxnId}" ) 
    public ResponseEntity<List<Object[]>> getAssociatedDebitEntries( 
                                         @PathVariable Integer creditTxnId ) {
        try {
            List<DebitCreditAssoc> associations = null ;
            List<Object[]> debitAssociations = new ArrayList<>() ;
            
            associations = dcaRepo.findByCreditTxnId( creditTxnId ) ;
            
            if( associations != null && !associations.isEmpty() ) {
                for( DebitCreditAssoc assoc : associations ) {
                    
                    LedgerEntry debitLE = null ;
                    debitLE = lRepo.findById( assoc.getDebitTxnId() ).get() ;
                    
                    Object[] tupule = new Object[3] ;
                    tupule[0] = assoc ;
                    tupule[1] = debitLE ;
                    tupule[2] = findSecondaryRecoveyAssociations( debitLE ) ;
                    
                    debitAssociations.add( tupule ) ;
                }
            }
            return status( HttpStatus.OK ).body( debitAssociations ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting debit association entries.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    @GetMapping( "/CreditAssociation/{debitTxnId}" ) 
    public ResponseEntity<List<Object[]>> getAssociatedCreditEntries( 
                                         @PathVariable Integer debitTxnId ) {
        try {
            List<DebitCreditAssoc> associations = null ;
            List<Object[]> creditAssociations = new ArrayList<>() ;
            
            associations = dcaRepo.findByDebitTxnId( debitTxnId ) ;
            
            if( associations != null && !associations.isEmpty() ) {
                for( DebitCreditAssoc assoc : associations ) {
                    
                    LedgerEntry creditLE = null ;
                    creditLE = lRepo.findById( assoc.getCreditTxnId() ).get() ;
                    
                    Object[] tupule = new Object[3] ;
                    tupule[0] = assoc ;
                    tupule[1] = creditLE ;
                    tupule[2] = findSecondaryRecoveyAssociations( creditLE ) ;
                    
                    creditAssociations.add( tupule ) ;
                }
            }
            return status( HttpStatus.OK ).body( creditAssociations ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting credit association entries.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
    
    private List<DebitCreditAssoc> findSecondaryRecoveyAssociations( LedgerEntry entry ) {
        
        List<DebitCreditAssoc> assocs = null ;
        if( entry.isCredit() ) {
            assocs = dcaRepo.findByCreditTxnId( entry.getId() ) ;
        }
        else {
            assocs = dcaRepo.findByDebitTxnId( entry.getId() ) ;
        }
        return assocs ;
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
    public ResponseEntity<List<DebitCreditAssoc>> save( 
                         @RequestBody List<DebitCreditAssoc> associations ) {
        try {
            List<DebitCreditAssoc> savedAssociations = new ArrayList<>() ;
            
            Iterable<DebitCreditAssoc> iterable = dcaRepo.saveAll( associations ) ;
            iterable.forEach( savedAssociations::add ) ;
            
            return status( HttpStatus.OK ).body( savedAssociations ) ;
        }
        catch( DataIntegrityViolationException d ) {
            log.error( "Error: Duplicate entry. " + d.getMessage(), d ) ;
            return status( HttpStatus.BAD_REQUEST )
                   .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving association entry.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @DeleteMapping( "/DebitCreditAssociation/{id}" ) 
    public ResponseEntity<APIMsgResponse> delete( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting association entry. " + id ) ;
            dcaRepo.deleteById( id ) ;
            
            return status( HttpStatus.OK )
                   .body( new APIMsgResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Deleting association entry.", e ) ;
            return status( HttpStatus.INTERNAL_SERVER_ERROR ).body( null ) ;
        }
    }
}
