package com.sandy.capitalyst.server.api.equity.market.recoengine.cfg;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = true )
public class AttributeComputerCfg extends AbstractCfg {

    private int weight = 0 ;
}
