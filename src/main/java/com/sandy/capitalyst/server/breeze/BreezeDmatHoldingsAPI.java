package com.sandy.capitalyst.server.breeze;

import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;

public class BreezeDmatHoldingsAPI {

    public void getDmatHoldings() throws Exception {
        
        String url = BreezeSession.BRZ_API_BASEURL + "/dematholdings" ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url ) ;
    }
}
