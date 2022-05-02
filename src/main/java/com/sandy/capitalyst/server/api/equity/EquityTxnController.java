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

import com.sandy.capitalyst.server.api.equity.helper.EquityTxnPosting ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

@RestController
public class EquityTxnController {

    private static final Logger log = Logger.getLogger( EquityTxnController.class ) ;
    private static final DecimalFormat DF = new DecimalFormat( "#.00" ) ;
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @PostMapping( "/Equity/Transaction" ) 
    public ResponseEntity<APIResponse> updateEquityHoldings(
                                @RequestBody List<EquityTxnPosting> txnPostings ) {
        try {
            log.debug( "Updating equity transactions" ) ;
            int numSaved = 0 ;
            for( EquityTxnPosting txnPosting : txnPostings ) {
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

    private boolean saveTransaction( EquityTxnPosting posting )
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
    
    private EquityTxn createEquityTxn( EquityTxnPosting posting,
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
    
    private String getPostingHash( EquityTxnPosting posting ) {
        
        StringBuilder builder = new StringBuilder() ;
        builder.append( posting.getOwnerName() )
               .append( SDF.format( posting.getTxnDate() ) )
               .append( posting.getAction() )
               .append( DF.format( posting.getQuantity() ) )
               .append( DF.format( posting.getTxnPrice() ) ) ;
        return StringUtil.getHash( builder.toString() ) ;
    }
}
