package com.sandy.capitalyst.server.api.ota.action.idirectmap;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.job.equity.idirectmap.ICICIDirectSymbolMappingJob ;

public class IDirectSymbolMappingOTA extends OTA {
    
    public static final String NAME = "MapICICIDirectSymbols" ;
    
    public IDirectSymbolMappingOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        ICICIDirectSymbolMappingJob job = new ICICIDirectSymbolMappingJob() ;
        job.mapICICIDirectSymbols( this ) ;
    }
}
