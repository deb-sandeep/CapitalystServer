package com.sandy.capitalyst.server.api.equity.recoengine;

import lombok.Data ;

@Data
public abstract class RecoAttributeEvaluator {
    
    public abstract void evaluate( EquityReco reco, 
                                   StatisticsManager statsMgr ) ;
}
