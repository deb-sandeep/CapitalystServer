package com.sandy.capitalyst.server.breeze.api;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;

public class BreezeGetTradeListAPI extends BreezeAPI {
    
    private static final Logger log = Logger.getLogger( BreezeGetTradeListAPI.class ) ;

    public void getTradeList() throws Exception {
        
        String url = BreezeSessionManager.BRZ_API_BASEURL + "/trades" ;
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "from_date", "2022-05-28T06:00:00.000Z" ) ;
        params.put( "to_date", "2022-09-07T06:00:00.000Z" ) ;
        params.put( "exchange_code", "NSE" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
