package com.sandy.capitalyst.server.dao.account;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.Transient ;

import lombok.Data ;

@Data
@Entity
@Table( name = "account" )
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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
    
    public Float getBalance() {
        if( this.balance == null ) {
            this.balance = 0F ;
        }
        return this.balance ;
    }
}
