package com.sandy.capitalyst.server.api.equity.recoengine.cfg;

import java.util.HashMap ;
import java.util.Map ;

import lombok.Data ;

@Data
public class AbstractCfg {
    
    private String id = null ;
    private Map<String, String> attributes = new HashMap<>() ;
}
