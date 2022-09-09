package com.sandy.capitalyst.server.breeze.api;

import java.util.Date ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHoldingsAPIResponse ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;

/**
 * Parameter        Data       Mandatory  Description
 * ---------------+----------+----------+---------------------
 * exchangeCode     String      No         "NSE", "NFO", "BSE" (NSE default)
 * fromDate         String      No         ISO 8601
 * toDate           String      No         ISO 8601
 * stockCode        String      No         "AXIBAN", "TATMOT"
 */
public class BreezeGetPortfolioHoldingsAPI 
    extends BreezeAPI<PortfolioHoldingsAPIResponse> {

    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/portfolioholdings" ;

    public static class PortfolioHoldingsAPIResponse extends BreezeAPIResponse {
    }
    
    protected BreezeGetPortfolioHoldingsAPI() {
        super( API_URL ) ;
        super.addParam( "exchange_code", "NSE" ) ;
    }
    
    public void setExchangeCode( String code ) {
        super.addParam( "exchange_code", code ) ;
    }
    
    public void setFromDate( Date date ) {
        super.addParam( "from_date", date ) ;
    }

    public void setToDate( Date date ) {
        super.addParam( "to_date", date ) ;
    }
    
    public void setStockCode( String code ) {
        super.addParam( "stock_code", code ) ;
    }
}
