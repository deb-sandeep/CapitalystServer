package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_holding" )
public class EquityHolding {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String ownerName   = null ;
    private String symbolIcici = null ;
    private String symbolNse   = null ;
    private String companyName = null ;
    private String isin        = null ;
    private int    quantity    = 0 ;
    
    @Column( precision=16 )
    private float avgCostPrice = 0.0f ;

    @Column( precision=16 )
    private float currentMktPrice = 0.0f ;

    @Column( precision=16 )
    private float realizedProfitLoss = 0.0f ;
    
    @Column( precision=16 )
    private float dayGain = 0.0f ;

    private Date lastUpdate = null ;
    
    public EquityHolding() {}
    
    protected EquityHolding( EquityHolding blueprint ) {
        this.id                 = blueprint.id ;
        this.ownerName          = blueprint.ownerName ;
        this.symbolIcici        = blueprint.symbolIcici ;
        this.symbolNse          = blueprint.symbolNse ;
        this.companyName        = blueprint.companyName ;
        this.isin               = blueprint.isin ;
        this.quantity           = blueprint.quantity ;
        this.avgCostPrice       = blueprint.avgCostPrice ;
        this.currentMktPrice    = blueprint.currentMktPrice ;
        this.realizedProfitLoss = blueprint.realizedProfitLoss ;
        this.dayGain            = blueprint.dayGain ;
        this.lastUpdate         = blueprint.lastUpdate ;
    }
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "EquityHolding [\n" ) ; 
        
        builder.append( "   id                 = " + id                 + "\n" ) ;
        builder.append( "   ownerName          = " + ownerName          + "\n" ) ;
        builder.append( "   symbolIcici        = " + symbolIcici        + "\n" ) ;
        builder.append( "   symbolNse          = " + symbolNse          + "\n" ) ;
        builder.append( "   companyName        = " + companyName        + "\n" ) ;
        builder.append( "   isin               = " + isin               + "\n" ) ;
        builder.append( "   quantity           = " + quantity           + "\n" ) ;
        builder.append( "   avgCostPrice       = " + avgCostPrice       + "\n" ) ;
        builder.append( "   currentMktPrice    = " + currentMktPrice    + "\n" ) ;
        builder.append( "   realizedProfitLoss = " + realizedProfitLoss + "\n" ) ;
        builder.append( "   dayGain            = " + dayGain            + "\n" ) ;
        builder.append( "   lastUpdate         = " + lastUpdate         + "\n" ) ;
        
        builder.append( "]" ) ;
        return builder.toString() ;
    }
}
