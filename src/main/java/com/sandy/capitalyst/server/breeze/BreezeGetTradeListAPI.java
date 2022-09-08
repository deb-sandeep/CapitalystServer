package com.sandy.capitalyst.server.breeze;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;

public class BreezeGetTradeListAPI {

    public void getTradeList() throws Exception {
        
        String url = BreezeSession.BRZ_API_BASEURL + "/trades" ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "from_date", "2022-05-28T06:00:00.000Z" ) ;
        params.put( "to_date", "2022-09-07T06:00:00.000Z" ) ;
        params.put( "exchange_code", "NSE" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
