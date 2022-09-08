package com.sandy.capitalyst.server.breeze;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.breeze.internal.BreezeNetworkClient ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;

public class BreezeGetQuotesAPI {

    public void getQuotes() throws Exception {
        
        String url = BreezeSession.BRZ_API_BASEURL + "/quotes" ;
        
        //"BAAUTO,BAFINS,BANBAR,YESBAN,HINAER,LARTOU,LTINFO,MINLIM,TATELX,TATSTE,NIFBEE,ADAGRE,ADATRA,ADAGAS,ADAPOR"
        
        Map<String, Object> params = new HashMap<>() ;
        params.put( "exchange_code", "NSE" ) ;
        params.put( "stock_code", "BAFINS" ) ;
        params.put( "product_type", "cash" ) ;
        
        BreezeNetworkClient netClient = BreezeNetworkClient.instance() ;
        netClient.get( url, params ) ;
    }
}
