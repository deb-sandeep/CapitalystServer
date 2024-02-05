package com.sandy.capitalyst.server.api.equity ;

import java.util.Date ;

import org.apache.commons.lang3.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityMonitor ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMonitorRepo ;

@RestController
public class EquityMonitorController {

    private static final Logger log = Logger.getLogger( EquityMonitorController.class ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private EquityMonitorRepo emonRepo = null ;
    
    @PostMapping( "/Equity/Monitor/{isin}" ) 
    public ResponseEntity<APIMsgResponse> addMonitor( @PathVariable String isin ) {
        try {
            EquityMaster em = emRepo.findByIsin( isin ) ;
            
            EquityMonitor mon = emonRepo.findByIsin( isin ) ;
            if( mon == null ) {
                mon = new EquityMonitor() ;
                mon.setDateAdded( new Date() ) ;
                mon.setIsin( isin ) ;
                mon.setSymbolIcici( em.getSymbolIcici() ) ;
                mon.setSymbolNse( em.getSymbol() ) ;
            }
            else {
                mon.setDateAdded( new Date() ) ;
            }
            
            emonRepo.save( mon ) ;
            
            RecoManager.instance().setMonitorFlag( mon, true ) ;

            return APIMsgResponse.success() ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving equity monitor.", e ) ;
            String stackTrace = ExceptionUtils.getStackTrace( e ) ;
            return APIMsgResponse.serverError( stackTrace ) ;
        }
    }

    @DeleteMapping( "/Equity/Monitor/{isin}" ) 
    public ResponseEntity<APIMsgResponse> deleteMonitor( @PathVariable String isin ) {
        try {
            EquityMonitor mon = emonRepo.findByIsin( isin ) ;
            if( mon != null ) {
                emonRepo.delete( mon ) ;
            }
            RecoManager.instance().setMonitorFlag( mon, false ) ;
            return APIMsgResponse.success() ;
        }
        catch( Exception e ) {
            log.error( "Error :: Deleting equity monitor.", e ) ;
            String stackTrace = ExceptionUtils.getStackTrace( e ) ;
            return APIMsgResponse.serverError( stackTrace ) ;
        }
    }
}
