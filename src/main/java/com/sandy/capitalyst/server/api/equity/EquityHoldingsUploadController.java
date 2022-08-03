package com.sandy.capitalyst.server.api.equity ;

import java.text.DecimalFormat ;
import java.text.SimpleDateFormat ;
import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.EquityTxnPostingVO ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Post - /Equity/Transaction
// @Post - /Equity/Holding

@RestController
public class EquityHoldingsUploadController {

    private static final Logger log = Logger.getLogger( EquityHoldingsUploadController.class ) ;
    private static final DecimalFormat DF = new DecimalFormat( "#.00" ) ;
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @PostMapping( "/Equity/Holding" ) 
    public ResponseEntity<APIResponse> updateEquityHoldings(
                                @RequestBody List<EquityHolding> holdings ) {
        try {
            log.debug( "Updating equity holdings" ) ;
            for( EquityHolding holding : holdings ) {
                saveHolding( holding ) ;
            }
            String msg = "Success. Updated " + holdings.size() + " records." ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving equity holding.", e ) ;
            String stackTrace = ExceptionUtils.getFullStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( stackTrace ) ) ;
        }
    }

    @PostMapping( "/Equity/Transaction" ) 
    public ResponseEntity<APIResponse> updateEquityTransactions(
                                @RequestBody List<EquityTxnPostingVO> txnPostings ) {
        try {
            log.debug( "Updating equity transactions" ) ;
            int numSaved = 0 ;
            for( EquityTxnPostingVO txnPosting : txnPostings ) {
                if( saveTransaction( txnPosting ) ) {
                    numSaved++ ;
                }
            }
            String msg = "Success. Saved " + numSaved + " txns." ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving equity holding.", e ) ;
            String stackTrace = ExceptionUtils.getFullStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( stackTrace ) ) ;
        }
    }

    private void saveHolding( EquityHolding postedHolding )
        throws Exception {
        
        EquityHolding existingHolding = ehRepo.findByOwnerNameAndSymbolIcici( 
                                            postedHolding.getOwnerName(), 
                                            postedHolding.getSymbolIcici() ) ;
        
        EquityHolding holding = null ;
        
        if( existingHolding == null ) {
            log.debug( "No existing asset found. Creating New." ) ;
            holding = postedHolding ;
        }
        else {
            log.debug( "Updating asset with posted values." ) ;
            existingHolding.setAvgCostPrice( postedHolding.getAvgCostPrice() ) ;
            existingHolding.setCompanyName( postedHolding.getCompanyName() ) ;
            existingHolding.setCurrentMktPrice( postedHolding.getCurrentMktPrice() ) ;
            existingHolding.setIsin( postedHolding.getIsin() ) ;
            existingHolding.setOwnerName( postedHolding.getOwnerName() ) ;
            existingHolding.setQuantity( postedHolding.getQuantity() ) ;
            existingHolding.setSymbolIcici( postedHolding.getSymbolIcici() ) ;
            existingHolding.setLastUpdate( postedHolding.getLastUpdate() ) ;
            holding = existingHolding ;
        }
        
        // Update the NSE symbol by looking up the ISIN Symbol map
        EquityMaster eqIsin = emRepo.findByIsin( postedHolding.getIsin() ) ;
        if( eqIsin != null ) {
            log.debug( "Updating NSE symbol." ) ;
            holding.setSymbolNse( eqIsin.getSymbol() ) ;
        }
        
        log.debug( "Updating equity holding" ) ;
        ehRepo.save( holding ) ;
    }
    
    private boolean saveTransaction( EquityTxnPostingVO posting )
        throws Exception {
        
        EquityTxn txn = null ;
        String hash = getPostingHash( posting ) ;
        
        txn = etRepo.findByHash( hash ) ;
        if( txn == null ) {
            txn = createEquityTxn( posting, hash ) ;
            etRepo.save( txn ) ;
            return true ;
        }
        return false ;
    }
    
    private EquityTxn createEquityTxn( EquityTxnPostingVO posting,
                                       String hash ) {
        
        EquityTxn txn = new EquityTxn() ;
        EquityHolding holding = ehRepo.findByOwnerNameAndSymbolIcici( 
                                                posting.getOwnerName(), 
                                                posting.getSymbolICICI() ) ;
        
        txn.setAction( posting.getAction() ) ;
        txn.setBrokerage( posting.getBrokerage() ) ;
        txn.setHash( hash ) ;
        txn.setHoldingId( holding.getId() ) ;
        txn.setQuantity( posting.getQuantity() ) ;
        txn.setStampDuty( posting.getStampDuty() ) ;
        txn.setTxnCharges( posting.getTxnCharges() ) ;
        txn.setTxnDate( posting.getTxnDate() ) ;
        txn.setTxnPrice( posting.getTxnPrice() ) ;
        
        return txn ;
    }
    
    private String getPostingHash( EquityTxnPostingVO posting ) {
        
        StringBuilder builder = new StringBuilder() ;
        builder.append( posting.getOwnerName() )
               .append( SDF.format( posting.getTxnDate() ) )
               .append( posting.getAction() )
               .append( DF.format( posting.getQuantity() ) )
               .append( DF.format( posting.getTxnPrice() ) ) ;
        return StringUtil.getHash( builder.toString() ) ;
    }
}
