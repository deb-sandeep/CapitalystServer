package com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.Normalizer ;

public class MarketCapNormalizer extends Normalizer {
    
    private static final float MIN_NORM_VALUE = 0.0F ;
    private static final float AVG_NORM_VALUE = 0.5F ;
    private static final float MAX_NORM_VALUE = 1.0F ;

    public MarketCapNormalizer() {
        super( "Market Cap", 
               MIN_NORM_VALUE, AVG_NORM_VALUE, MAX_NORM_VALUE ) ;
    }
    
    @Override
    public float normalize( EquityReco reco, StatisticsManager statsMgr ) {
        
        if( reco.getEquityMaster().isEtf() ) {
            return 1.00F ;
        }

        return threePointNormalize( reco.getIndicators().getMarketCap(), 
                                    statsMgr.getMktCapStats() ) ;
    }
}
