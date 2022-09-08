package com.sandy.capitalyst.server.breeze;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;

public class BreezeGetTradeDetailAPI {

    public void getTradeDetail() throws Exception {
        
        String url = BreezeSession.BRZ_API_BASEURL + "/trades" ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "exchange_code", "NSE" ) ;
        params.put( "order_id", "20220830N800018122" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
