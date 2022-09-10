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
@Table( name = "equity_master" )
public class EquityMaster {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin = null ;
    private String symbol = null ;
    private String symbolIcici = null ;
    private String name = null ;
    private String mcName = null ;
    
    @Column( name = "is_etf" )
    private boolean etf = false ;
    
    private String industry = null ;
    private String detailUrl = null ;
    
    private String description = null ;
}