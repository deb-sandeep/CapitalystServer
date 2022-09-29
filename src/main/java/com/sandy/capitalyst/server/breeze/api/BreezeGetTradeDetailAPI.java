package com.sandy.capitalyst.server.breeze.api;

import java.util.Date ;

import com.fasterxml.jackson.annotation.JsonFormat ;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties ;
import com.fasterxml.jackson.annotation.JsonProperty ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeConstants.ExchangeCode ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI.TradeDetail ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIProxy ;

import lombok.Data ;

/**
 * Parameter        Data       Mandatory  Description
 * ---------------+----------+----------+---------------------
 * exchange_code    String      Yes        "NSE","NFO","BSE
 * order_id         String      Yes        order_id as obtained from /trade
 */
public class BreezeGetTradeDetailAPI 
    extends BreezeAPIProxy<TradeDetail> {

    private static final String API_URL = Breeze.BRZ_API_BASEURL + "/trades" ;

    /*
    "trade_id" : "2022/0919/87016717",
    "exchange_trade_time" : "19-Sep-2022 15:13:11"
    "stock_code" : "NIFBEE",
    "action" : "S",
    "executed_quantity" : "3",
    "execution_price" : "192.65",
    "brokerage_amount" : "1.27",
    "total_transaction_cost" : "1.5",
    "taxes" : "0.2494",
    "settlement_id" : "2022177",
    "exchange_trade_id" : "54435643",
    */
        
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TradeDetail {
        
        @JsonProperty( "trade_id" )
        private String tradeId = null ; 
        
        @JsonProperty( "exchange_trade_time" )
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy HH:mm:ss" )
        private Date txnDate = null ;
        
        @JsonProperty( "stock_code" )
        private String symbolIcici = null ;

        @JsonProperty( "action" )
        private String action = null ;

        @JsonProperty( "executed_quantity" )
        private int quantity = 0 ;

        @JsonProperty( "execution_price" )        
        private float txnPrice = 0.0f ;
        
        @JsonProperty( "total_transaction_cost" )
        private float brokerage = 0.0f ;

        @JsonProperty( "taxes" )
        private float stampDuty = 0.0f ;
        
        @JsonProperty( "settlement_id" )
        private String settlementId = null ;
        
        @JsonProperty( "exchange_trade_id" )
        private String exchangeTradeId = null ;
        
        public String getAction() {
            return action.equalsIgnoreCase( "S" ) ? "Sell" : "Buy" ;
        }
        
        public float getTxnCharges() {
            return (float)(0.0032/100)*(txnPrice * quantity) ;
        }
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
