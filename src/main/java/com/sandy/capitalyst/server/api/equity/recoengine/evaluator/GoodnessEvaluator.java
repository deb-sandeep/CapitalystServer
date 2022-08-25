package com.sandy.capitalyst.server.api.equity.recoengine.evaluator;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.RecoAttributeEvaluator ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.BetaNormalizer ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.CAGRNormalizer ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.MCEssentialsNormalizer ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.MarketCapNormalizer ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.PENormalizer ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm.PiotroskiNormalizer ;

public class GoodnessEvaluator extends RecoAttributeEvaluator {
    
    static final Logger log = Logger.getLogger( GoodnessEvaluator.class ) ;
    
    private Normalizer piotroskiNorm = new PiotroskiNormalizer   (  5 ) ;
    private Normalizer betaNorm      = new BetaNormalizer        ( 10 ) ;
    private Normalizer mcEssNorm     = new MCEssentialsNormalizer( 15 ) ;
    private Normalizer peNorm        = new PENormalizer          ( 15 ) ;
    private Normalizer mktCapNorm    = new MarketCapNormalizer   ( 20 ) ;
    private Normalizer cagrNorm      = new CAGRNormalizer        ( 35 ) ;
    
    private Normalizer[] normalizers = {
        mktCapNorm,
        betaNorm,
        cagrNorm,
        peNorm,
        piotroskiNorm,
        mcEssNorm
    } ;

    @Override
    public void evaluate( EquityReco reco, StatisticsManager statsMgr ) {
        
        float goodnessScore = 0 ;
        
        for( Normalizer n : normalizers ) {
            
            float normVal = n.normalize( reco, statsMgr ) ;
            float weight  = n.getWeight() ;
            float score   = ( normVal * weight ) ;
            
            goodnessScore += score ;
        }
        
        reco.setGoodnessScore( goodnessScore ) ;
    }
}
