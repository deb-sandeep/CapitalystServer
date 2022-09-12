package com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.BetaNormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.CAGRNormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.MCEssentialsNormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.MarketCapNormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.PENormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm.PiotroskiNormalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.RecoAttributeEvaluator ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.StatisticsManager ;

import lombok.Getter ;
import lombok.Setter ;

public class GoodnessEvaluator extends RecoAttributeEvaluator {
    
    static final Logger log = Logger.getLogger( GoodnessEvaluator.class ) ;
    
    private Normalizer piotroskiNorm = new PiotroskiNormalizer() ;
    private Normalizer betaNorm      = new BetaNormalizer() ;
    private Normalizer mcEssNorm     = new MCEssentialsNormalizer() ;
    private Normalizer peNorm        = new PENormalizer() ;
    private Normalizer mktCapNorm    = new MarketCapNormalizer() ;
    private Normalizer cagrNorm      = new CAGRNormalizer() ;
    
    private Normalizer[] normalizers = {
        mktCapNorm,
        betaNorm,
        cagrNorm,
        peNorm,
        piotroskiNorm,
        mcEssNorm
    } ;
    
    @Getter @Setter
    private int piotroskiScoreWt = 16 ;
    
    @Getter @Setter
    private int betaWt = 16 ;
    
    @Getter @Setter
    private int mcEssentialScoreWt = 17 ;
    
    @Getter @Setter
    private int peWt = 17 ;
    
    @Getter @Setter
    private int mktCapWt = 17 ;
    
    @Getter @Setter
    private int cagrWt = 17 ;
    
    public void initialize() {
        
        log.debug( "   - piotroskiScoreWt   = " + piotroskiScoreWt ) ;
        log.debug( "   - betaWt             = " + betaWt ) ;
        log.debug( "   - mcEssentialScoreWt = " + mcEssentialScoreWt ) ;
        log.debug( "   - peWt               = " + peWt ) ;
        log.debug( "   - mktCapWt           = " + mktCapWt ) ;
        log.debug( "   - cagrWt             = " + cagrWt ) ;
    }
    
    @Override
    public void evaluate( EquityReco reco, StatisticsManager statsMgr ) {
        
        piotroskiNorm.setWeight( piotroskiScoreWt   ) ;
        betaNorm.setWeight     ( betaWt             ) ;
        mcEssNorm.setWeight    ( mcEssentialScoreWt ) ;
        peNorm.setWeight       ( peWt               ) ;
        mktCapNorm.setWeight   ( mktCapWt           ) ;
        cagrNorm.setWeight     ( cagrWt             ) ;
        
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
