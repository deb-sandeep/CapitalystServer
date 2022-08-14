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

import com.sandy.capitalyst.server.api.equity.vo.MCNameISIN ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

// @Post - /Equity/Master/MCNameISIN

@RestController
public class MCEquityNameISINMapController {

    private static final Logger log = Logger.getLogger( MCEquityNameISINMapController.class ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @PostMapping( "/Equity/Master/MCNameISIN" ) 
    public ResponseEntity<APIResponse> updateMCNameISINMapping(
                                @RequestBody List<MCNameISIN> mappings ) {
        try {
            log.debug( "Updating equity master with MC Name ISIN mappings" ) ;
            
            for( MCNameISIN mapping : mappings ) {
                saveMapping( mapping ) ;
            }
            
            String msg = "Success. Updated " + mappings.size() + " records." ;
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
    
    private void saveMapping( MCNameISIN mapping )
        throws Exception {
        
        EquityMaster em = emRepo.findByIsin( mapping.getIsin() ) ;
        if( em != null ) {
            em.setMfName( mapping.getMcName() ) ;
            em.setDetailUrl( mapping.getDetailURL() ) ;
            emRepo.save( em ) ;
        }
    }
}
