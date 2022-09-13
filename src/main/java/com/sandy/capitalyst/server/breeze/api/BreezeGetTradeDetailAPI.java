package com.sandy.capitalyst.server.breeze.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.ExchangeCode ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI.TradeDetail ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;

import lombok.Data ;

/**
 * Parameter        Data       Mandatory  Description
 * ---------------+----------+----------+---------------------
 * exchange_code    String      Yes        "NSE","NFO","BSE
 * order_id         String      Yes        order_id as obtained from /trade
 */
public class BreezeGetTradeDetailAPI 
    extends BreezeAPI<TradeDetail> {

    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/trades" ;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TradeDetail {
    }
    
    public BreezeGetTradeDetailAPI() {
        super( "TradeDetail", API_URL, TradeDetail.class ) ;
        setExchangeCode( ExchangeCode.NSE ) ;
        addMandatoryParameter( "order_id" ) ;
    }
    
    public void setExchangeCode( ExchangeCode code ) {
        super.addParam( "exchange_code", code.toString() ) ;
    }
    
    public void setOrderId( String id ) {
        super.addParam( "order_id", id ) ;
    }
}
