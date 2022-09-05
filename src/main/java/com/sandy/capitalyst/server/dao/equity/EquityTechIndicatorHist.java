package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_tech_indicator_hist" )
public class EquityTechIndicatorHist {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin           = null ;
    private String symbolNse      = null ;
    private Date   asOnDate       = null ;
    private String name           = null ;
    private float  level          = 0 ;
    private String indication     = null ;

    public EquityTechIndicatorHist() {}
    
    public EquityTechIndicatorHist( EquityTechIndicator ind ) {
        copy( ind ) ;
    }
    
    public void copy( EquityTechIndicator ind ) {
        this.isin       = ind.getIsin() ;
        this.symbolNse  = ind.getSymbolNse() ;
        this.asOnDate   = ind.getAsOnDate() ;
        this.name       = ind.getName() ;
        this.level      = ind.getLevel() ;
        this.indication = ind.getIndication() ;
    }
}