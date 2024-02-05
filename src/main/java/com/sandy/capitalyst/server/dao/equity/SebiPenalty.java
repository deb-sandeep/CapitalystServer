package com.sandy.capitalyst.server.dao.equity;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "sebi_penalty" )
public class SebiPenalty {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String orderId = null ;
    
    @Column( precision=16 )
    private float penaltyAmt = 0.0F ;
}
