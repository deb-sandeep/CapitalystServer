package com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.norm;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator.Normalizer ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.StatisticsManager ;

public class PiotroskiNormalizer extends Normalizer {
    
    private static final float MIN_NORM_VALUE = 0.0F ;
    private static final float AVG_NORM_VALUE = 0.5F ;
    private static final float MAX_NORM_VALUE = 1.0F ;

    public PiotroskiNormalizer() {
        super( "Piotroski",
               MIN_NORM_VALUE, AVG_NORM_VALUE, MAX_NORM_VALUE ) ;
    }
    
    @Override
    public float normalize( EquityReco reco, StatisticsManager statsMgr ) {
        
        if( reco.getEquityMaster().isEtf() ) {
            return 1.00F ;
        }

        return threePointNormalize( reco.getIndicators().getPiotroskiScore(), 
                                    statsMgr.getPiotroskiStats() ) ;
    }
}
