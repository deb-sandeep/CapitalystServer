package com.sandy.capitalyst.server.api.equity.recoengine.evaluator;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.StatisticsManager ;

import lombok.Getter ;

public abstract class Normalizer {
    
    @Getter 
    private float weight = 0 ;
    
    @Getter
    private String name = null ;
    
    public Normalizer( String name, float weight ) {
        this.name = name ;
        this.weight = weight ;
    }

    public abstract float normalize( EquityReco reco, StatisticsManager statsMgr ) ;
}
