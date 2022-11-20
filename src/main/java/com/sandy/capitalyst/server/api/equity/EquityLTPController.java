package com.sandy.capitalyst.server.api.equity ;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository.LTP ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;

// @Get - /Equity/LTP

@RestController
public class EquityLTPController {

    private static final Logger log = Logger.getLogger( EquityLTPController.class ) ;
    
    @Autowired
    private EquityLTPRepository ltpRepo = null ;
    
    @GetMapping( "/Equity/LTP" ) 
    public ResponseEntity<Map<String, LTP>> getLTPs() {
        
        Map<String, LTP> ltps = new HashMap<>() ;
        try {
            RecoManager.instance().getRecommendations().forEach( reco -> {
                LTP ltp = ltpRepo.getLTP( reco.getSymbolNse() ) ;
                if( ltp != null ) {
                    ltps.put( ltp.getSymbolNse(), ltp ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( ltps ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting LTPs.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
