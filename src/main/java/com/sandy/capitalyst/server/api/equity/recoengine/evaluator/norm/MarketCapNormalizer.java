package com.sandy.capitalyst.server.api.equity.recoengine.evaluator.norm;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;
import com.sandy.capitalyst.server.api.equity.recoengine.evaluator.Normalizer ;

public class MarketCapNormalizer extends Normalizer {

    public MarketCapNormalizer( float weight ) {
        super( "Market Cap", weight ) ;
    }
    
    @Override
    public float normalize( EquityReco reco, StatisticsManager statsMgr ) {
        
        if( reco.getEquityMaster().isEtf() ) {
            return 1.00F ;
        }
        return 1.00F ;
    }
}
