package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import com.sandy.capitalyst.server.api.equity.vo.StockIndicators ;
import com.sandy.capitalyst.server.api.equity.vo.StockIndicators.TechIndicator ;
import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_tech_indicator" )
public class EquityTechIndicator implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "etiPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "equity_tech_indicator_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="etiPkGen" )
    private Integer id = null ;
    
    private String isin           = null ;
    private String symbolNse      = null ;
    private Date   asOnDate       = null ;
    private String name           = null ;
    private float  level          = 0 ;
    private String indication     = null ;

    public EquityTechIndicator() {}
    
    public EquityTechIndicator( StockIndicators ind, TechIndicator ti ) {
        copy( ind, ti ) ;
    }
    
    public void copy( StockIndicators ind, TechIndicator ti ) {
        this.isin       = ind.getIsin() ;
        this.symbolNse  = ind.getSymbolNse() ;
        this.asOnDate   = ind.getAsOnDate() ;
        this.name       = ti.getName() ;
        this.level      = ti.getLevel() ;
        this.indication = ti.getIndication() ;
    }
}
