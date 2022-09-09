package com.sandy.capitalyst.server.breeze.api;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;

public class BreezeGetTradeDetailAPI extends BreezeAPI {

    private static final Logger log = Logger.getLogger( BreezeGetTradeDetailAPI.class ) ;

    public void getTradeDetail() throws Exception {
        
        String url = BreezeSessionManager.BRZ_API_BASEURL + "/trades" ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "exchange_code", "NSE" ) ;
        params.put( "order_id", "20220830N800018122" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
