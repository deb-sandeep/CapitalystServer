package com.sandy.capitalyst.server.breeze.api;

import java.util.Date ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties ;
import com.fasterxml.jackson.annotation.JsonProperty ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.ExchangeCode ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIProxy ;

import lombok.Data ;

/**
 * Parameter        Data       Mandatory  Description
 * ---------------+----------+----------+---------------------
 * exchangeCode     String      No         "NSE", "NFO", "BSE" (NSE default)
 * fromDate         String      No         ISO 8601
 * toDate           String      No         ISO 8601
 * stockCode        String      No         "AXIBAN", "TATMOT"
 */
public class BreezeGetPortfolioHoldingsAPI 
    extends BreezeAPIProxy<PortfolioHolding> {

    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/portfolioholdings" ;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortfolioHolding {
        
        @JsonProperty( "stock_code" )
        private String symbol = null ;

        @JsonProperty( "quantity" )
        private int quantity = 0 ;

        @JsonProperty( "average_price" )
        private float averagePrice = 0.0F ;

        @JsonProperty( "current_market_price" )
        private float currentMktPrice = 0.0F ;
        
        @JsonProperty( "change_percentage" ) 
        private float changePercentage = 0.0F ;
        
        public float getChange() {
            float prevClose = ( 100 * currentMktPrice )/( 100 + changePercentage ) ;
            return currentMktPrice - prevClose ;
        }
    }
    
    public BreezeGetPortfolioHoldingsAPI() {
        super( "GetPortfolioHoldings", API_URL, PortfolioHolding.class ) ;
        setExchangeCode( ExchangeCode.NSE ) ;
    }
    
    public void setExchangeCode( ExchangeCode code ) {
        super.addParam( "exchange_code", code.toString() ) ;
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
