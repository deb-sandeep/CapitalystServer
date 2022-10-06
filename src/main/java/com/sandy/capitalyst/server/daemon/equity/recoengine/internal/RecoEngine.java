package com.sandy.capitalyst.server.daemon.equity.recoengine.internal;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco.Type ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.Screener.ScreenerResult ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityMonitor ;
import com.sandy.capitalyst.server.dao.equity.EquityTTMPerf ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMonitorRepo ;

public class RecoEngine extends RecoEngineBase {

    private static final Logger log = Logger.getLogger( RecoEngine.class ) ;
    
    private static RecoEngine instance = null ;
    
    public static RecoEngine instance() throws Exception {
        if( instance == null ) {
            instance = new RecoEngine() ;
        }
        return instance ;
    }
    
    private EquityMonitorRepo monRepo = null ;

    private RecoEngine() {
        monRepo = getBean( EquityMonitorRepo.class ) ;
    }
    
    public EquityReco screen( EquityMaster em,
                              EquityIndicators eIndicators,
                              EquityTTMPerf ttmPerf ) {
        
        String                    symbolNse   = eIndicators.getSymbolNse() ;
        EquityReco                reco        = new EquityReco() ;
        List<EquityHolding>       holdings    = null ;
        List<EquityTechIndicator> tIndicators = null ;

        log.debug( "Generating recommendations for " + symbolNse ) ; 
        
        tIndicators = etiRepo.findBySymbolNse( symbolNse ) ;
        holdings = ehRepo.findNonZeroHoldingsForNSESymbol( symbolNse ) ;
        
        if( holdings != null && !holdings.isEmpty() ) {
            reco.setHoldings( holdings ) ;
        }
        
        reco.setEquityMaster( em ) ;
        reco.setIndicators( eIndicators ) ;
        reco.setTechIndicators( tIndicators ) ;
        reco.setTtmPerf( ttmPerf ) ;
        
        applyScreeners( eIndicators, tIndicators, reco ) ;

        return reco ;
    }
    
    private void applyScreeners( EquityIndicators eInds, 
                                 List<EquityTechIndicator> tInds,
                                 EquityReco reco ) {
        
        ScreenerResult result = null ;
        EquityMonitor mon = null ;
        
        mon = monRepo.findBySymbolNse( reco.getEquityMaster().getSymbol() ) ;
        if( mon != null ) {
            reco.setMonitored( true ) ;
            return ;
        }
        
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

    public void applyEvaluators( EquityReco reco,
                                 StatisticsManager statsMgr ) {
        
        for( RecoAttributeEvaluator eval : super.evaluators ) {
            eval.evaluate( reco, statsMgr ) ;
        }
    }
}
