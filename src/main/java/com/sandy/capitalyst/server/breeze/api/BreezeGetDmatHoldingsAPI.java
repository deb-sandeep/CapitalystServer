package com.sandy.capitalyst.server.breeze.api;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI.DmatHoldingsAPIResponse ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;

public class BreezeGetDmatHoldingsAPI extends BreezeAPI<DmatHoldingsAPIResponse> {
    
    static final Logger log = Logger.getLogger( BreezeGetDmatHoldingsAPI.class ) ;
    
    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/dematholdings" ;

//    "stock_code" : "LARTOU",
//    "stock_ISIN" : "INE018A01030",
//    "quantity" : "120",
//    "demat_total_bulk_quantity" : "120",
//    "demat_avail_quantity" : "0",
//    "blocked_quantity" : "0",
//    "demat_allocated_quantity" : "120"
    public static class DmatHoldingsAPIResponse extends BreezeAPIResponse {
    }
    
    public BreezeGetDmatHoldingsAPI() {
        super( API_URL ) ;
    }
}
