package com.sandy.capitalyst.server.dao.ledger;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

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
