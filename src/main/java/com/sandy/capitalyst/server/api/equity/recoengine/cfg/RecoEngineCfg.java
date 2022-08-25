package com.sandy.capitalyst.server.api.equity.recoengine.cfg;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Data ;

@Data
public class RecoEngineCfg {

    private List<ScreenerCfg> screenerCfgs = new ArrayList<>() ;
    private List<AttributeEvaluatorCfg> attributeEvaluatorCfgs = new ArrayList<>() ;
}
