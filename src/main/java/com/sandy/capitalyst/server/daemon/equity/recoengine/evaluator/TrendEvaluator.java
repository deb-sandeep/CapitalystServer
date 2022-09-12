package com.sandy.capitalyst.server.daemon.equity.recoengine.evaluator;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.RecoAttributeEvaluator ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.StatisticsManager ;

public class TrendEvaluator extends RecoAttributeEvaluator {
    
    static final Logger log = Logger.getLogger( TrendEvaluator.class ) ;
    
    @Override    
    public void initialize() {
    }
    
    @Override
    public void evaluate( EquityReco reco, StatisticsManager statsMgr ) {
    }
}
