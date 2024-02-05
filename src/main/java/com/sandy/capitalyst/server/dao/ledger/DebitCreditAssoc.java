package com.sandy.capitalyst.server.dao.ledger;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "debit_credit_association" )
public class DebitCreditAssoc {
    
    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id ;
    
    private Integer debitTxnId = null ;
    private Integer creditTxnId = null ;
    
    private float amount = 0 ;
    private String note = null ;
}
