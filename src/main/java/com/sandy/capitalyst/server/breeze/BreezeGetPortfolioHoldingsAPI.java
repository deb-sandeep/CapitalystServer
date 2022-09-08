package com.sandy.capitalyst.server.breeze;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;

public class BreezeGetPortfolioHoldingsAPI {

    public void getPortfolioHoldings() throws Exception {
        
        String url = BreezeSession.BRZ_API_BASEURL + "/portfolioholdings" ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "exchange_code", "NSE" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
