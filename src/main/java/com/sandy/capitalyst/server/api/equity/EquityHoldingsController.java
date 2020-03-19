package com.sandy.capitalyst.server.api.equity ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVO ;
import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityISIN ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityISINRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

@RestController
public class EquityHoldingsController {

    private static final Logger log = Logger.getLogger( EquityHoldingsController.class ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityTxnRepo etRepo = null ;
    
    @Autowired
    private EquityISINRepo eiRepo = null ;
    
    @GetMapping( "/Equity/Holding" ) 
    public ResponseEntity<List<EquityHoldingVO>> getEquityHoldings() {
        
        log.debug( "Getting equity portfolio" ) ;
        List<EquityHoldingVO> voHoldings = new ArrayList<>() ;
        EquityHoldingVOBuilder voBuilder = new EquityHoldingVOBuilder() ;
        
        try {
            for( EquityHolding dbHolding : ehRepo.findAll() ) {
                List<EquityTxn> txns = null ;
                if( dbHolding.getQuantity() > 0 ) {
                    txns = etRepo.findByHoldingIdOrderByTxnDateAsc( dbHolding.getId() ) ;
                }
                voHoldings.add( voBuilder.buildVO( dbHolding, txns ) ) ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( voHoldings ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
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
        EquityISIN eqIsin = eiRepo.findByIsin( postedHolding.getIsin() ) ;
        if( eqIsin != null ) {
            log.debug( "Updating NSE symbol." ) ;
            holding.setSymbolNse( eqIsin.getSymbol() ) ;
        }
        
        ehRepo.save( holding ) ;
    }
}
