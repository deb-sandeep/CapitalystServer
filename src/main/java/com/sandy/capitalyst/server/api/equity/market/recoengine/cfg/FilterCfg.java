package com.sandy.capitalyst.server.api.equity.market.recoengine.cfg;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = true )
public class FilterCfg extends AbstractCfg {

    private int priority = 0 ;
    private float lowerLimit = 0 ;
    private float upperLimit = 0 ;
    private String range = null ;
}
