package com.sandy.capitalyst.server.daemon.equity.recoengine.internal;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;

import lombok.Data ;

@Data
public abstract class RecoAttributeEvaluator {
    
    public abstract void evaluate( EquityReco reco, 
                                   StatisticsManager statsMgr ) ;
    
    public void initialize() throws Exception {}
}
