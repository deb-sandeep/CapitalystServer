package com.sandy.capitalyst.server.api.equity.recoengine;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.Recommendation.Type ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;

public class RecoManager {

    private static final Logger log = Logger.getLogger( RecoManager.class ) ;
    
    private static RecoManager instance = null ;
    
    private RecoEngine recoEngine = null ;
    private Map<String, Recommendation> recommendations = new TreeMap<>() ;
    private List<Recommendation> screenedStockRecos = new ArrayList<>() ;

    private EquityIndicatorsRepo eiRepo = null ;
    
    
    public static RecoManager instance() throws Exception {
        if( instance == null ) {
            instance = new RecoManager() ;
        }
        
        instance.initialize() ;

        return instance ;
    }
    
    private RecoManager() throws Exception {
        this.recoEngine = RecoEngine.instance() ;
    }
    
    private void initialize() throws Exception {
        if( needsInitialization() ) {
            this.recoEngine.initialize() ;
            this.eiRepo = getBean( EquityIndicatorsRepo.class ) ;
            refreshRecommendationsCache() ;
        }
    }
    
    private boolean needsInitialization() throws Exception {
        return this.recoEngine.needsInitiaization() ;
    }
    
    private void refreshRecommendationsCache() throws Exception {
        
        log.debug( "Refreshing recommendation manager cache" ) ;
        
        Iterable<EquityIndicators> inds = eiRepo.findAll() ;
        Recommendation reco = null ;
        
        recommendations.clear() ;
        screenedStockRecos.clear() ;
        
        for( EquityIndicators ind : inds ) {
            
            reco = recoEngine.getRecommendation( ind.getSymbolNse() ) ;
            recommendations.put( ind.getSymbolNse(), reco ) ;
            
            if( reco.getType() != Type.ACCEPTANCE_CRITERIA_NOT_MET ) {
                screenedStockRecos.add( reco ) ;
            }
        }
        
        printStats() ;
    }
    
    private void printStats() {
        
        List<String> rejectedStocks = new ArrayList<>() ;
        List<String> screenedStocks = new ArrayList<>() ;
        
        for( String symbolNse : recommendations.keySet() ) {
            Recommendation reco = recommendations.get( symbolNse ) ;
            if( reco.getType() == Type.ACCEPTANCE_CRITERIA_NOT_MET ) {
                rejectedStocks.add( StringUtils.rightPad( symbolNse, 15 ) + 
                                    " - " + reco.getMessage() ) ;
            }
            else {
                screenedStocks.add( symbolNse ) ;
            }
        }
        
        log.debug( "Recommendation manager statistics." ) ;
        log.debug( "----------------------------------" ) ;
        
        log.debug( "  Num recommendations = " + recommendations.size() ) ;
        log.debug( "  Screened stocks     = " + screenedStocks.size() ) ;
        log.debug( "  Rejected stocks     = " + rejectedStocks.size() ) ;
        
        for( String msg : rejectedStocks ) {
            log.debug( "    " + msg ) ;
        }
        
    }
    
    public List<Recommendation> getScreenedStockRecommendations() {
        return screenedStockRecos ;
    }
}
