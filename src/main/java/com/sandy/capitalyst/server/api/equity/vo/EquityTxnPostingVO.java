package com.sandy.capitalyst.server.api.equity.vo;

import java.util.Date ;

import lombok.Data ;

@Data
public class EquityTxnPostingVO {

    private String ownerName   = null ;
    private String symbolICICI = null ;
    private String action      = null ;
    private int    quantity    = 0 ;
    private Date   txnDate     = null ;
    private float  txnPrice    = 0.0f ;
    private float  brokerage   = 0.0f ;
    private float  txnCharges  = 0.0f ;
    private float  stampDuty   = 0.0f ;
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "EquityTxn [\n" ) ; 
        
        builder.append( "   ownerName  = " + this.ownerName + "\n" ) ;
        builder.append( "   symbol     = " + this.symbolICICI + "\n" ) ;
        builder.append( "   action     = " + this.action + "\n" ) ;
        builder.append( "   quantity   = " + this.quantity + "\n" ) ;
        builder.append( "   txnPrice   = " + this.txnPrice + "\n" ) ;
        builder.append( "   brokerage  = " + this.brokerage + "\n" ) ;
        builder.append( "   txnCharges = " + this.txnCharges + "\n" ) ;
        builder.append( "   stampDuty  = " + this.stampDuty + "\n" ) ;
        builder.append( "   txnDate    = " + this.txnDate + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}