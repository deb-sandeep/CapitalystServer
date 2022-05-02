package com.sandy.capitalyst.server.api.ota.action.emrefresh;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.core.util.ListedEquitiesRefresher ;

public class EquityMasterRefreshOTA extends OTA {
    
    //private static final String LISTED_ETF_URL = 
    //        "https://www1.nseindia.com/content/indices/ind_nifty500list.csv" ;
    
    public static final String NAME = "RefreshEquityMaster" ;
    
    public EquityMasterRefreshOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        ListedEquitiesRefresher leParser = new ListedEquitiesRefresher( this ) ;
        leParser.refreshEquityMaster() ;
    }
}
