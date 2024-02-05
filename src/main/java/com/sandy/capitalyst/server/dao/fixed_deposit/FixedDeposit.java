package com.sandy.capitalyst.server.dao.fixed_deposit;

import java.util.Date ;

import jakarta.persistence.CascadeType ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.JoinColumn ;
import jakarta.persistence.ManyToOne ;
import jakarta.persistence.Table ;

import com.sandy.capitalyst.server.dao.account.Account ;

@Entity
@Table( name = "fixed_deposit" )
public class FixedDeposit {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    @ManyToOne( cascade= {CascadeType.ALL} )
    @JoinColumn( name="base_account_id" )
    private Account baseAccount = null ;
    
    @ManyToOne
    @JoinColumn( name="parent_account_id" )
    private Account parentAccount = null ;
    
    private Date openDate = null ;
    private Date matureDate = null ;
    private int openAmt = 0 ;
    private int matureAmt = 0 ;
    private boolean autoRenew = false ;
    private float interestRate = 0.0f ;
    private int recurringAmt = 0 ;
    private int recurringDom = 0 ;

    public FixedDeposit() {}

    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setBaseAccount( Account val ) {
        this.baseAccount = val ;
    }
        
    public Account getBaseAccount() {
        return this.baseAccount ;
    }

    public void setOpenDate( Date val ) {
        this.openDate = val ;
    }
        
    public Date getOpenDate() {
        return this.openDate ;
    }

    public void setMatureDate( Date val ) {
        this.matureDate = val ;
    }
        
    public Date getMatureDate() {
        return this.matureDate ;
    }

    public void setOpenAmt( int val ) {
        this.openAmt = val ;
    }
        
    public int getOpenAmt() {
        return this.openAmt ;
    }

    public void setMatureAmt( int val ) {
        this.matureAmt = val ;
    }
        
    public int getMatureAmt() {
        return this.matureAmt ;
    }

    public void setAutoRenew( boolean val ) {
        this.autoRenew = val ;
    }
        
    public boolean isAutoRenew() {
        return this.autoRenew ;
    }

    public void setInterestRate( float val ) {
        this.interestRate = val ;
    }
        
    public float getInterestRate() {
        return this.interestRate ;
    }

    public void setRecurringAmt( int val ) {
        this.recurringAmt = val ;
    }
        
    public int getRecurringAmt() {
        return this.recurringAmt ;
    }

    public void setRecurringDom( int val ) {
        this.recurringDom = val ;
    }
        
    public int getRecurringDom() {
        return this.recurringDom ;
    }
    
    public Account getParentAccount() {
        return parentAccount ;
    }

    public void setParentAccount( Account parentAccount ) {
        this.parentAccount = parentAccount ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "FixedDeposit [\n" ) ; 

        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   baseAccount = " + this.baseAccount + "\n" ) ;
        builder.append( "   parentAccount = " + this.parentAccount + "\n" ) ;
        builder.append( "   openDate = " + this.openDate + "\n" ) ;
        builder.append( "   matureDate = " + this.matureDate + "\n" ) ;
        builder.append( "   openAmt = " + this.openAmt + "\n" ) ;
        builder.append( "   matureAmt = " + this.matureAmt + "\n" ) ;
        builder.append( "   autoRenew = " + this.autoRenew + "\n" ) ;
        builder.append( "   interestRate = " + this.interestRate + "\n" ) ;
        builder.append( "   recurringAmt = " + this.recurringAmt + "\n" ) ;
        builder.append( "   recurringDom = " + this.recurringDom + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
