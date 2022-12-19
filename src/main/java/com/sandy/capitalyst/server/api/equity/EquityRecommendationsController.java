package com.sandy.capitalyst.server.api.equity ;

import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository.LTP ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;

// @Get - /Equity/Recommendations

@RestController
public class EquityRecommendationsController {

    private static final Logger log = Logger.getLogger( EquityRecommendationsController.class ) ;
    
    @Autowired
    private EquityLTPRepository ltpRepo = null ;
    
    @GetMapping( "/Equity/Recommendations" ) 
    public ResponseEntity<List<EquityReco>> getRecommendations() {
        
        LTP ltp = null ;
        List<EquityReco> recommendations = null ;
        
        try {
            RecoManager recoMgr = RecoManager.instance() ;
            
            recommendations = recoMgr.getRecommendations() ;
            
            for( EquityReco reco : recommendations ) {
                ltp = ltpRepo.getLTP( reco.getEquityMaster().getSymbol() ) ;
                if( ltp != null ) {
                    reco.setLtp( ltp ) ;
                    reco.getIndicators().setCurrentPrice( ltp.getPrice() ) ;
                }
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( recommendations ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
