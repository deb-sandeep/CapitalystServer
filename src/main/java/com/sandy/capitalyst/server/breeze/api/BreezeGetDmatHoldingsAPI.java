package com.sandy.capitalyst.server.breeze.api;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties ;
import com.fasterxml.jackson.annotation.JsonProperty ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI.DmatHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;

import lombok.Data ;

public class BreezeGetDmatHoldingsAPI extends BreezeAPI<DmatHolding> {
    
    static final Logger log = Logger.getLogger( BreezeGetDmatHoldingsAPI.class ) ;
    
    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/dematholdings" ;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DmatHolding {
        
        @JsonProperty( "stock_code" )
        private String symbol = null ;
        
        @JsonProperty( "stock_ISIN" )
        private String isin = null ;
        
        @JsonProperty( "quantity" )
        private int quantity = 0 ;
    }
    
    public BreezeGetDmatHoldingsAPI() {
        super( "GetDmatHoldings", API_URL, DmatHolding.class ) ;
    }
}
