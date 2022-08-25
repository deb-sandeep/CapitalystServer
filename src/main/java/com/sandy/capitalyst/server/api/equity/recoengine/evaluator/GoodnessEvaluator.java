package com.sandy.capitalyst.server.api.equity.recoengine.evaluator;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.RecoAttributeEvaluator ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.MarketCapNormalizer ;

public class GoodnessEvaluator extends RecoAttributeEvaluator {
    
    private static final Logger log = Logger.getLogger( GoodnessEvaluator.class ) ;
    
    private Normalizer mktCapNorm = new MarketCapNormalizer( 10 ) ;
    
    private Normalizer[] normalizers = {
        mktCapNorm
    } ;

    @Override
    public void evaluate( EquityReco reco, StatisticsManager statsMgr ) {
        
        float goodnessScore = 0 ;
        
        for( Normalizer n : normalizers ) {
            
            float normVal = n.normalize( reco, statsMgr ) ;
            float weight  = n.getWeight() ;
            float score   = ( normVal * weight ) ;
            
            log.debug( StringUtils.rightPad( reco.getIndicators().getSymbolNse(), 15 ) + 
                       " - " + score ) ;
            
            goodnessScore += score ;
        }
        
        reco.setGoodnessScore( goodnessScore ) ;
    }
}
