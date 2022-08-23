package com.sandy.capitalyst.server.api.equity ;

import java.time.Month ;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Comparator ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquitySellTxnVOListBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.EquitySellTxnVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Get - /Equity/Transactions/Sell?fy=2022

@RestController
public class EquitySellTxnQueryController {

    private static final Logger log = Logger.getLogger( EquitySellTxnQueryController.class ) ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @GetMapping( "/Equity/Transactions/Sell" ) 
    public ResponseEntity<List<EquitySellTxnVO>> getEquityTxns( 
                   @RequestParam( name = "fy", required = false ) Integer fy ) {
        
        log.debug( "Getting sell equity transactions." ) ;
        
        Date[] dateRange = null ;
        List<Integer> holdingsSold = null ;
        List<EquitySellTxnVO> sellTxns = new ArrayList<>() ;
        
        try {
            dateRange = getDateRange( fy ) ;
            holdingsSold = etRepo.getHoldingsSold( dateRange[0], dateRange[1] ) ;
            for( Integer holdingId : holdingsSold ) {
                collateSellTxns( holdingId, sellTxns ) ;
            }
            
            sellTxns.sort( new Comparator<EquitySellTxnVO>() {

                @Override
                public int compare( EquitySellTxnVO s1, EquitySellTxnVO s2 ) {
                    return s2.getTxnDate().compareTo( s1.getTxnDate() ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( sellTxns ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity transactions.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    private Date[] getDateRange( Integer fy ) {
        
        int financialYear = getFinancialYear( fy ) ;
        
        Calendar startCal = Calendar.getInstance() ;
        startCal.set( financialYear, Calendar.APRIL, 1, 0, 0, 0 ) ;
        
        Calendar endCal = Calendar.getInstance() ;
        endCal.set( financialYear+1, Calendar.MARCH, 31, 23, 59, 59 ) ;
        
        Date[] range = { startCal.getTime(), endCal.getTime() } ;
        return range ;
    }

    private int getFinancialYear( Integer fy ) {
        
        int financialYear = 0 ;
        
        if( fy == null ) {
            Calendar cal = Calendar.getInstance() ;
            int month = cal.get( Calendar.MONTH ) ;
            
            financialYear = cal.get( Calendar.YEAR ) ;
            
            if( month < Month.APRIL.getValue() ) {
                financialYear -= 1 ;
            }
        }
        else if( fy < 100 ) {
            financialYear = 2000 + fy ;
        }
        else {
            financialYear = fy ;
        }
        
        return financialYear ;
    }
    
    private void collateSellTxns( Integer holdingId, 
                                  List<EquitySellTxnVO> sellTxns ) 
        throws Exception {
        
        EquityHolding eh = null ;
        List<EquityTxn> txnList = null ;
        EquitySellTxnVOListBuilder helper = null ;
        
        eh = ehRepo.findById( holdingId ).get() ;
        txnList = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( holdingId ) ;

        helper = new EquitySellTxnVOListBuilder() ;
        sellTxns.addAll( helper.buildSellTxnVOList( eh, txnList ) ) ;
    }
}