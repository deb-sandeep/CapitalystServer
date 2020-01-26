package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "equity_txn" )
public class EquityTxn {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private int holdingId = 0 ;
    private String action = null ;
    private int quantity = 0 ;
    private Date txnDate = null ;
    private String hash = null ;

    @Column( precision=16, scale=2 )
    private float txnPrice = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float brokerage = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float txnCharges = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float stampDuty = 0.0f ;
    
    public EquityTxn() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setHoldingId( int val ) {
        this.holdingId = val ;
    }
        
    public int getHoldingId() {
        return this.holdingId ;
    }

    public void setAction( String val ) {
        this.action = val ;
    }
        
    public String getAction() {
        return this.action ;
    }

    public void setQuantity( int val ) {
        this.quantity = val ;
    }
        
    public int getQuantity() {
        return this.quantity ;
    }

    public void setTxnPrice( float val ) {
        this.txnPrice = val ;
    }
        
    public float getTxnPrice() {
        return this.txnPrice ;
    }

    public void setBrokerage( float val ) {
        this.brokerage = val ;
    }
        
    public float getBrokerage() {
        return this.brokerage ;
    }

    public void setTxnCharges( float val ) {
        this.txnCharges = val ;
    }
        
    public float getTxnCharges() {
        return this.txnCharges ;
    }

    public void setStampDuty( float val ) {
        this.stampDuty = val ;
    }
        
    public float getStampDuty() {
        return this.stampDuty ;
    }

    public void setTxnDate( Date val ) {
        this.txnDate = val ;
    }
        
    public Date getTxnDate() {
        return this.txnDate ;
    }

    public void setHash( String val ) {
        this.hash = val ;
    }
        
    public String getHash() {
        return this.hash ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "EquityTxn [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   holdingId = " + this.holdingId + "\n" ) ;
        builder.append( "   action = " + this.action + "\n" ) ;
        builder.append( "   quantity = " + this.quantity + "\n" ) ;
        builder.append( "   txnPrice = " + this.txnPrice + "\n" ) ;
        builder.append( "   brokerage = " + this.brokerage + "\n" ) ;
        builder.append( "   txnCharges = " + this.txnCharges + "\n" ) ;
        builder.append( "   stampDuty = " + this.stampDuty + "\n" ) ;
        builder.append( "   txnDate = " + this.txnDate + "\n" ) ;
        builder.append( "   hash = " + this.hash + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}