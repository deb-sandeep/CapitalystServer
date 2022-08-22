package com.sandy.capitalyst.server.api.equity ;

import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Get - /Equity/Transactions/Sell?fy=2022
// @Get - /Equity/Transactions/Buy?fy=2022

@RestController
public class EquityTransactionsQueryController {

    private static final Logger log = Logger.getLogger( EquityTransactionsQueryController.class ) ;
    
    private static final String SELL_TXN_TYPE = "Sell" ;
    private static final String BUY_TXN_TYPE = "Buy" ;
    
    @SuppressWarnings( "unused" )
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @SuppressWarnings( "unused" )
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @GetMapping( "/Equity/Transactions/{txnType}" ) 
    public ResponseEntity<List<IndividualEquityHoldingVO>> getEquityTxns( 
                         @PathVariable( "txnType" ) String txnType,
                         @RequestParam( "fy"      ) Integer financialYear ) {
        
        log.debug( "Getting equity transactions. " + 
                   "Type = " + txnType + ", FY = " + financialYear ) ;
        
        
        try {
            if( txnType.equals( SELL_TXN_TYPE ) ) {
                
            }
            else if( txnType.equals( BUY_TXN_TYPE ) ) {
                
            }
            else {
                throw new Exception( "Invalid txnType = " + txnType ) ;
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( null ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity transactions.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
