package com.sandy.capitalyst.server.dao.equity;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import com.sandy.capitalyst.server.api.equity.vo.StockIndicators ;
import com.sandy.capitalyst.server.api.equity.vo.StockIndicators.TechIndicator ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_tech_indicator" )
public class EquityTechIndicator {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin           = null ;
    private String symbolNse      = null ;
    private String name           = null ;
    private float  level          = 0 ;
    private String indication      = null ;

    public EquityTechIndicator() {}
    
    public EquityTechIndicator( StockIndicators ind, TechIndicator ti ) {
        copy( ind, ti ) ;
    }
    
    public void copy( StockIndicators ind, TechIndicator ti ) {
        this.isin       = ind.getIsin() ;
        this.symbolNse  = ind.getSymbolNse() ;
        this.name       = ti.getName() ;
        this.level      = ti.getLevel() ;
        this.indication = ti.getIndication() ;
    }
}