package com.sandy.capitalyst.server.api.ota.action.emrefresh;

import com.sandy.capitalyst.server.api.ota.action.OTA ;

public class EquityMasterRefreshOTA extends OTA {
    
    public static final String NAME = "RefreshEquityMaster" ;
    
    public EquityMasterRefreshOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        addResult( "Refreshing ETF symbols" ) ;
        ListedETFRefresher etfRefresher = new ListedETFRefresher( this ) ;
        etfRefresher.refreshEquityMaster() ;
        addResult( "ETF symbols refreshed" ) ;
        
        addResult( "Refreshing EQ symbols" ) ;
        ListedEquitiesRefresher eqRefresher = new ListedEquitiesRefresher( this ) ;
        eqRefresher.refreshEquityMaster() ;
        addResult( "EQ symbols refreshed" ) ;
    }
}
