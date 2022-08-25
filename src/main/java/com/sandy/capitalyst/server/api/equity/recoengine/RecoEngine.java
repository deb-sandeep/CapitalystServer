package com.sandy.capitalyst.server.api.equity.recoengine;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco.Type ;
import com.sandy.capitalyst.server.api.equity.recoengine.Screener.ScreenerResult ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

class RecoEngine extends RecoEngineBase {

    private static final Logger log = Logger.getLogger( RecoEngine.class ) ;
    
    private static RecoEngine instance = null ;
    
    public static RecoEngine instance() throws Exception {
        if( instance == null ) {
            instance = new RecoEngine() ;
        }
        return instance ;
    }

    private RecoEngine() {}
    
    public EquityReco getRecommendation( String symbolNse ) 
        throws Exception {
        
        EquityReco                reco        = new EquityReco() ;
        EquityMaster              em          = null ;
        EquityIndicators          eIndicators = null ;
        List<EquityHolding>       holdings    = null ;
        List<EquityTechIndicator> tIndicators = null ;

        log.debug( "Generating recommendations for " + symbolNse ) ; 
        
        em          = emRepo.findBySymbol( symbolNse ) ;
        eIndicators = eiRepo.findBySymbolNse( symbolNse ) ;
        tIndicators = etiRepo.findBySymbolNse( symbolNse ) ;
        
        holdings = ehRepo.findNonZeroHoldingsForNSESymbol( symbolNse ) ;
        if( holdings != null && !holdings.isEmpty() ) {
            reco.setHoldings( holdings ) ;
        }
        
        reco.setEquityMaster( em ) ;
        reco.setIndicators( eIndicators ) ;
        reco.setTechIndicators( tIndicators ) ;
        
        if( eIndicators == null ) {
            reco.setReco( Type.SCREENED_OUT, 
                          "Stock indicators not found for " + symbolNse ) ;
            return reco ;
        }
        
        applyScreeners( eIndicators, tIndicators, reco ) ;

        if( reco.getType() != Type.SCREENED_OUT ) {
            
        }
        
        return reco ;
    }
    
    private void applyScreeners( EquityIndicators eInds, 
                                 List<EquityTechIndicator> tInds,
                                 EquityReco reco ) {
        
        ScreenerResult result = null ;
        
        for( Screener filter : screeners ) {
            
            result = filter.screen( eInds, tInds, reco ) ;
            
            if( result.getResult() == ScreenerResult.REJECT ) {
                log.debug( "     REJECTED. " + filter.getId() + 
                           ". Msg = " + result.getDescription() ) ;
                
                reco.setReco( Type.SCREENED_OUT,
                              result.getDescription() ) ;
                break ;
            }
            else if( result.getResult() == ScreenerResult.ACCEPT ) {
                log.debug( "     ACCEPTED. " + filter.getId() ) ;
                break ;
            }
            else if( result.getResult() == ScreenerResult.NO_CARE ) {
                continue ;
            }
        }
    }
}
