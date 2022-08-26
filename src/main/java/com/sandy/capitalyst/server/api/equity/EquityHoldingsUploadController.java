package com.sandy.capitalyst.server.api.equity ;

import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

// @Post - /Equity/Holding

@RestController
public class EquityHoldingsUploadController {

    private static final Logger log = Logger.getLogger( EquityHoldingsUploadController.class ) ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
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
            
            RecoManager.instance().setEquityDataUpdated( true ) ;
            
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
            holding.setSymbolNse( eqIsin.getSymbol() ) ;
        }
        
        log.debug( "Updating equity holding" ) ;
        ehRepo.save( holding ) ;
    }
}
