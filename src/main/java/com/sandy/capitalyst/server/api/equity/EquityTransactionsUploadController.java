package com.sandy.capitalyst.server.api.equity ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang3.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.transaction.annotation.Transactional ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.EquityTxnPostingVO ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Post - /Equity/Transaction

@RestController
@Transactional
public class EquityTransactionsUploadController {

    private static final Logger log = Logger.getLogger( EquityTransactionsUploadController.class ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @PostMapping( "/Equity/Transaction" ) 
    public ResponseEntity<APIMsgResponse> updateEquityTransactions(
                                @RequestBody List<EquityTxnPostingVO> txnPostings ) {
        try {
            log.debug( "Updating equity transactions" ) ;
            
            Map<Integer, List<EquityTxn>> holdingTxnsMap = null ;
            
            log.debug( "Creating holding txns map." ) ;
            holdingTxnsMap = createHoldingTxnsMap( txnPostings ) ;
            
            log.debug( "Refreshing transactions." ) ;
            for( Integer holdingId : holdingTxnsMap.keySet() ) {
                
                log.debug( "   Refreshing txns for holding = " + holdingId ) ;
                etRepo.deleteByHoldingId( holdingId ) ;
                etRepo.saveAll( holdingTxnsMap.get( holdingId ) ) ;
            }
            
            RecoManager.instance().setEquityDataUpdated( true ) ;

            String msg = "Success. Saved " + txnPostings.size() + " txns." ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving equity holding.", e ) ;
            String stackTrace = ExceptionUtils.getStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( stackTrace ) ) ;
        }
    }
    
    private Map<Integer, List<EquityTxn>> createHoldingTxnsMap(
                                        List<EquityTxnPostingVO> txnPostings ) {
        
        Map<Integer, List<EquityTxn>> map = new HashMap<>() ;
        List<EquityTxn> txns = null ;
        EquityTxn txn = null ;
        
        for( EquityTxnPostingVO posting : txnPostings ) {
            
            txn = createEquityTxn( posting ) ;
            
            txns = map.get( txn.getHoldingId() ) ;
            if( txns == null ) {
                txns = new ArrayList<>() ;
                map.put( txn.getHoldingId(), txns ) ;
            }
            
            txns.add( txn ) ;
        }
        
        return map ;
    }

    private EquityTxn createEquityTxn( EquityTxnPostingVO posting ) {
        
        EquityTxn txn = new EquityTxn() ;
        EquityHolding holding = ehRepo.findByOwnerNameAndSymbolIcici( 
                                                posting.getOwnerName(), 
                                                posting.getSymbolICICI() ) ;
        
        txn.setAction( posting.getAction() ) ;
        txn.setBrokerage( posting.getBrokerage() ) ;
        txn.setHoldingId( holding.getId() ) ;
        txn.setQuantity( posting.getQuantity() ) ;
        txn.setStampDuty( posting.getStampDuty() ) ;
        txn.setTxnCharges( posting.getTxnCharges() ) ;
        txn.setTxnDate( posting.getTxnDate() ) ;
        txn.setTxnPrice( posting.getTxnPrice() ) ;
        
        return txn ;
    }
}
