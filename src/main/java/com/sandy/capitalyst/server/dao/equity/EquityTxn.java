package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_txn" )
public class EquityTxn {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private int    holdingId = 0 ;
    private String action    = null ;
    private int    quantity  = 0 ;
    private Date   txnDate   = null ;
    private String hash      = null ;

    @Column( precision=16, scale=2 )
    private float txnPrice = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float brokerage = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float txnCharges = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float stampDuty = 0.0f ;
    
    public EquityTxn() {}
    
    public EquityTxn( EquityTxn blueprint ) {
        
        this.id         = blueprint.id ;
        this.holdingId  = blueprint.holdingId ;
        this.action     = blueprint.action ;
        this.quantity   = blueprint.quantity ;
        this.txnPrice   = blueprint.txnPrice ;
        this.brokerage  = blueprint.brokerage ;
        this.txnCharges = blueprint.txnCharges ;
        this.stampDuty  = blueprint.stampDuty ;
        this.txnDate    = blueprint.txnDate ;
        this.hash       = blueprint.hash ;
    }
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "EquityTxn [\n" ) ; 
        
        builder.append( "   id         = " + id         + "\n" ) ;
        builder.append( "   holdingId  = " + holdingId  + "\n" ) ;
        builder.append( "   action     = " + action     + "\n" ) ;
        builder.append( "   quantity   = " + quantity   + "\n" ) ;
        builder.append( "   txnPrice   = " + txnPrice   + "\n" ) ;
        builder.append( "   brokerage  = " + brokerage  + "\n" ) ;
        builder.append( "   txnCharges = " + txnCharges + "\n" ) ;
        builder.append( "   stampDuty  = " + stampDuty  + "\n" ) ;
        builder.append( "   txnDate    = " + txnDate    + "\n" ) ;
        builder.append( "   hash       = " + hash       + "\n" ) ;
        
        builder.append( "]" ) ;
        return builder.toString() ;
    }
}