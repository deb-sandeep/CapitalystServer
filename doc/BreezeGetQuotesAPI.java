package com.sandy.capitalyst.server.breeze.api;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;

public class BreezeGetQuotesAPI extends BreezeAPI {

    private static final Logger log = Logger.getLogger( BreezeGetQuotesAPI.class ) ;

    public void getQuotes() throws Exception {
        
        String url = BreezeSessionManager.BRZ_API_BASEURL + "/quotes" ;
        
        //"BAAUTO,BAFINS,BANBAR,YESBAN,HINAER,LARTOU,LTINFO,MINLIM,TATELX,TATSTE,NIFBEE,ADAGRE,ADATRA,ADAGAS,ADAPOR"
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "exchange_code", "NSE" ) ;
        params.put( "stock_code", "BAFINS" ) ;
        params.put( "product_type", "cash" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
