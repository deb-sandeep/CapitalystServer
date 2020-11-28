package com.sandy.capitalyst.server.dao.account;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;
import javax.persistence.Transient ;

@Entity
@Table( name = "account" )
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id ;
    
    private boolean deleted = false ;
    private String accountOwner = null ;
    private String accountType = null ;
    private String accountNumber = null ;
    private Float  balance = null ;
    private String shortName = null ;
    private String bankName = null ;
    private String bankBranch = null ;
    private String description = null ;
    
    @Transient
    private Float  depositBalance = 0.0F ;
    
    public Integer getId() {
        return id ;
    }
    
    public void setId( Integer id ) {
        this.id = id ;
    }
    
    public boolean isDeleted() {
    	return deleted ;
    }
    
    public void setDeleted( boolean deleted ) {
    	this.deleted = deleted ;
    }
    
    public String getAccountOwner() {
        return accountOwner ;
    }

    public void setAccountOwner( String accountOwner ) {
        this.accountOwner = accountOwner ;
    }

    public String getAccountNumber() {
        return accountNumber ;
    }

    public void setAccountNumber( String accountNumber ) {
        this.accountNumber = accountNumber ;
    }

    public String getAccountType() {
        return accountType ;
    }

    public void setAccountType( String accountType ) {
        this.accountType = accountType ;
    }

    public String getShortName() {
        return shortName ;
    }

    public void setShortName( String shortName ) {
        this.shortName = shortName ;
    }
    
    public String getBankName() {
        return bankName ;
    }

    public void setBankName( String bankName ) {
        this.bankName = bankName ;
    }

    public String getBankBranch() {
        return bankBranch ;
    }

    public void setBankBranch( String bankBranch ) {
        this.bankBranch = bankBranch ;
    }

    public String getDescription() {
        return description ;
    }

    public void setDescription( String description ) {
        this.description = description ;
    }

    public Float getBalance() {
        return balance ;
    }

    public void setBalance( Float balance ) {
        this.balance = balance ;
    }

    public Float getDepositBalance() {
        return depositBalance ;
    }

    public void setDepositBalance( Float balance ) {
        this.depositBalance = balance ;
    }
}
