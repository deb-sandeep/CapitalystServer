package com.sandy.capitalyst.server.api.equity.market.recoengine.cfg;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Data ;

@Data
public class RecoEngineCfg {

    private List<FilterCfg> filterCfgs = new ArrayList<>() ;
    private List<AttributeComputerCfg> attributeComputerCfgs = new ArrayList<>() ;
}
