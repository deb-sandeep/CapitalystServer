package com.sandy.capitalyst.server.dao.equity;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "sebi_penalty" )
public class SebiPenalty {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String orderId = null ;
    
    @Column( precision=16, scale=2 )
    private float penaltyAmt = 0.0F ;
}