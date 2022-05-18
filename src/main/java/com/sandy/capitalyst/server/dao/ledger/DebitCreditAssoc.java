package com.sandy.capitalyst.server.dao.ledger;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.ManyToOne ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "debit_credit_association" )
public class DebitCreditAssoc {
    
    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id ;
    
    @ManyToOne
    @JoinColumn( name="debit_txn_id" )
    private LedgerEntry debitEntry ;
    
    @ManyToOne
    @JoinColumn( name="credit_txn_id" )
    private LedgerEntry creditEntry ;
    
    private float amount = 0 ;
    private String note = null ;
}
