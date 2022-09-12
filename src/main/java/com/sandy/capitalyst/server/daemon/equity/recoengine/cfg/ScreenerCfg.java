package com.sandy.capitalyst.server.daemon.equity.recoengine.cfg;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = true )
public class ScreenerCfg extends AbstractCfg {

    private int priority = 0 ;
    private float lowerLimit = 0 ;
    private float upperLimit = 0 ;
    private String range = null ;
}
