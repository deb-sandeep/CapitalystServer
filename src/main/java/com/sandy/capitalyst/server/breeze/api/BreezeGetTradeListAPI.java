package com.sandy.capitalyst.server.breeze.api;

import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.Action ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.ExchangeCode ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.ProductType ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI.Trade ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPI ;

import lombok.Data ;

/**
 * Parameter        Data       Mandatory  Description
 * ---------------+----------+----------+---------------------
 * from_date        String      Yes        ISO 8601
 * to_date          String      Yes        ISO 8601
 * exchange_code    String      Yes        "NSE","NFO","BSE
 * product_type     String      No         "futures","options","furtureplus","futureplus_sltp","optionplus","cash","eatm","btst","margin","marginplus"
 * action           String      No         "buy","sell"
 * stock_code       String      No         "AXIBAN","TATMOT
 */
public class BreezeGetTradeListAPI 
    extends BreezeAPI<Trade> {

    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/trades" ;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trade {
    }
    
    public BreezeGetTradeListAPI() {
        super( API_URL, Trade.class ) ;
        setExchangeCode( ExchangeCode.NSE ) ;
        populateDefaultDateRange() ;
    }
    
    private void populateDefaultDateRange() {
        
        Date now      = new Date() ;
        Date monthAgo = DateUtils.addMonths( now, -1 ) ;
        
        setFromDate( monthAgo ) ;
        setToDate( now ) ;
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
    
    public void setAction( Action action ) {
        super.addParam( "action", action.toString() ) ;
    }
    
    public void setProductType( ProductType type ) {
        super.addParam( "product_type", type.toString() ) ;
    }
}
