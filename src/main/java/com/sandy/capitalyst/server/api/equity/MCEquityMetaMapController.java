package com.sandy.capitalyst.server.api.equity ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.MCStockMeta ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

// @Post - /Equity/Master/MCStockMeta

@RestController
public class MCEquityMetaMapController {

    private static final Logger log = Logger.getLogger( MCEquityMetaMapController.class ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @PostMapping( "/Equity/Master/MCStockMeta" ) 
    public ResponseEntity<APIResponse> updateMCNameISINMapping(
                                @RequestBody MCStockMeta meta ) {
        try {
            log.debug( "Updating equity master with MC Name ISIN mappings" ) ;
            
            saveMapping( meta ) ;
            RecoManager.instance().setEquityDataUpdated( true ) ;

            String msg = "Success. Updated meta for " + meta.getSymbolNSE() ;
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
    
    private void saveMapping( MCStockMeta meta )
        throws Exception {
        
        EquityMaster em = emRepo.findByIsin( meta.getIsin() ) ;
        
        if( em != null ) {
            em.setMcName( meta.getMcName() ) ;
            em.setDetailUrl( meta.getDetailURL() ) ;
            em.setDescription( meta.getDescription() ) ;
            emRepo.save( em ) ;
        }
    }
}
