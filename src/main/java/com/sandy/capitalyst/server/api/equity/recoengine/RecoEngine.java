package com.sandy.capitalyst.server.api.equity.recoengine;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.Recommendation.Type ;
import com.sandy.capitalyst.server.api.equity.recoengine.Screener.ScreenerResult ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
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
    
    public Recommendation getRecommendation( String symbolNse ) 
        throws Exception {
        
        EquityIndicators eIndicators = null ;
        List<EquityTechIndicator> tIndicators = null ;
        Recommendation reco = new Recommendation() ;

        log.debug( "Generating recommendations for " + symbolNse ) ; 
        
        eIndicators = eiRepo.findBySymbolNse( symbolNse ) ;
        tIndicators = etiRepo.findBySymbolNse( symbolNse ) ;
        
        if( eIndicators == null ) {
            reco.setReco( Type.ACCEPTANCE_CRITERIA_NOT_MET, 
                           "Stock indicators not found for " + symbolNse,
                           eIndicators, tIndicators ) ;
            return reco ;
        }
        
        applyFilters( eIndicators, tIndicators, reco ) ;

        if( reco.getType() != Type.ACCEPTANCE_CRITERIA_NOT_MET ) {
            
        }
        
        return reco ;
    }
    
    private void applyFilters( EquityIndicators eInds, 
                               List<EquityTechIndicator> tInds,
                               Recommendation recos ) {
        
        ScreenerResult result = null ;
        
        for( Screener filter : screeners ) {
            
            result = filter.screen( eInds, tInds, recos ) ;
            
            if( result.getResult() == ScreenerResult.REJECT ) {
                log.debug( "     REJECTED. " + filter.getId() + 
                           ". Msg = " + result.getDescription() ) ;
                
                recos.setReco( Type.ACCEPTANCE_CRITERIA_NOT_MET,
                               result.getDescription(),
                               eInds, tInds ) ;
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
