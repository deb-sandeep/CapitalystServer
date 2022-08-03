package com.sandy.capitalyst.server.api.equity ;

import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.EquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

// @Get - /Equity/Holding 

@RestController
public class EquityHoldingsController {

    private static final Logger log = Logger.getLogger( EquityHoldingsController.class ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @GetMapping( "/Equity/Holding" ) 
    public ResponseEntity<List<EquityHoldingVO>> getEquityHoldings() {
        
        log.debug( "Getting equity portfolio" ) ;
        List<EquityHoldingVO> voHoldings = new ArrayList<>() ;
        EquityHoldingVOBuilder voBuilder = new EquityHoldingVOBuilder() ;
        
        try {
            for( EquityHolding dbHolding : ehRepo.findAll() ) {
                List<EquityTxn> txns = null ;
                if( dbHolding.getQuantity() > 0 ) {
                    txns = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( dbHolding.getId() ) ;
                    EquityHoldingVO vo = voBuilder.buildVO( dbHolding, txns ) ;
                    if( vo.getQuantity() > 0 ) {
                    	voHoldings.add( vo ) ;
                    }
                }
            }
            
            voHoldings.sort( new Comparator<EquityHoldingVO>() {
                public int compare( EquityHoldingVO o1, EquityHoldingVO o2 ) {
                    return o1.getCompanyName().compareTo( o2.getCompanyName() ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( voHoldings ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
