package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "mf_txn" )
public class MutualFundTxn {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private Integer mfId = null ;
    private String txnType = null ;
    private String txnChannel = null ;
    private Date txnDate = null ;
    
    @Column( precision=16, scale=2 )
    private float navPerUnit = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float numUnits = 0.0f ;
    
    @Column( precision=16, scale=2 )
    private float amount = 0.0f ;
    
    private String hash = null ;

    public MutualFundTxn() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }
    
    public Integer getMfId() {
        return mfId ;
    }

    public void setMfId( Integer mfId ) {
        this.mfId = mfId ;
    }

    public void setTxnType( String val ) {
        this.txnType = val ;
    }
        
    public String getTxnType() {
        return this.txnType ;
    }

    public void setTxnChannel( String val ) {
        this.txnChannel = val ;
    }
        
    public String getTxnChannel() {
        return this.txnChannel ;
    }

    public void setTxnDate( Date val ) {
        this.txnDate = val ;
    }
        
    public Date getTxnDate() {
        return this.txnDate ;
    }

    public void setNavPerUnit( float val ) {
        this.navPerUnit = val ;
    }
        
    public float getNavPerUnit() {
        return this.navPerUnit ;
    }

    public void setNumUnits( float val ) {
        this.numUnits = val ;
    }
        
    public float getNumUnits() {
        return this.numUnits ;
    }

    public void setAmount( float val ) {
        this.amount = val ;
    }
        
    public float getAmount() {
        return this.amount ;
    }
    
    public String getHash() {
        return hash ;
    }

    public void setHash( String hash ) {
        this.hash = hash ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "MutualFundTxn [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   mfId = " + this.mfId + "\n" ) ;
        builder.append( "   txnType = " + this.txnType + "\n" ) ;
        builder.append( "   txnChannel = " + this.txnChannel + "\n" ) ;
        builder.append( "   txnDate = " + this.txnDate + "\n" ) ;
        builder.append( "   navPerUnit = " + this.navPerUnit + "\n" ) ;
        builder.append( "   numUnits = " + this.numUnits + "\n" ) ;
        builder.append( "   amount = " + this.amount + "\n" ) ;
        builder.append( "   hash = " + this.hash + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}