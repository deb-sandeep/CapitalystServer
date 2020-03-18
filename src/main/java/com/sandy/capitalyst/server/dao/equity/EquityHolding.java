package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "equity_holding" )
public class EquityHolding {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String ownerName = null ;
    private String symbolIcici = null ;
    private String symbolNse = null ;
    private String companyName = null ;
    private String isin = null ;
    private int quantity = 0 ;
    
    @Column( precision=16, scale=2 )
    private float avgCostPrice = 0.0f ;

    @Column( precision=16, scale=2 )
    private float currentMktPrice = 0.0f ;

    @Column( precision=16, scale=2 )
    private float realizedProfitLoss = 0.0f ;
    
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
        this.lastUpdate         = blueprint.lastUpdate ;
    }
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setOwnerName( String val ) {
        this.ownerName = val ;
    }
        
    public String getOwnerName() {
        return this.ownerName ;
    }

    public void setSymbolIcici( String val ) {
        this.symbolIcici = val ;
    }
        
    public String getSymbolIcici() {
        return this.symbolIcici ;
    }

    public void setSymbolNse( String val ) {
        this.symbolNse = val ;
    }
        
    public String getSymbolNse() {
        return this.symbolNse ;
    }

    public void setCompanyName( String val ) {
        this.companyName = val ;
    }
        
    public String getCompanyName() {
        return this.companyName ;
    }

    public void setIsin( String val ) {
        this.isin = val ;
    }
        
    public String getIsin() {
        return this.isin ;
    }

    public void setQuantity( int val ) {
        this.quantity = val ;
    }
        
    public int getQuantity() {
        return this.quantity ;
    }

    public void setAvgCostPrice( float val ) {
        this.avgCostPrice = val ;
    }
        
    public float getAvgCostPrice() {
        return this.avgCostPrice ;
    }

    public void setCurrentMktPrice( float val ) {
        this.currentMktPrice = val ;
    }
        
    public float getCurrentMktPrice() {
        return this.currentMktPrice ;
    }

    public void setRealizedProfitLoss( float val ) {
        this.realizedProfitLoss = val ;
    }
        
    public float getRealizedProfitLoss() {
        return this.realizedProfitLoss ;
    }
    
    public Date getLastUpdate() {
        return lastUpdate ;
    }

    public void setLastUpdate( Date lastUpdate ) {
        this.lastUpdate = lastUpdate ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "EquityHolding [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   ownerName = " + this.ownerName + "\n" ) ;
        builder.append( "   symbolIcici = " + this.symbolIcici + "\n" ) ;
        builder.append( "   symbolNse = " + this.symbolNse + "\n" ) ;
        builder.append( "   companyName = " + this.companyName + "\n" ) ;
        builder.append( "   isin = " + this.isin + "\n" ) ;
        builder.append( "   quantity = " + this.quantity + "\n" ) ;
        builder.append( "   avgCostPrice = " + this.avgCostPrice + "\n" ) ;
        builder.append( "   currentMktPrice = " + this.currentMktPrice + "\n" ) ;
        builder.append( "   realizedProfitLoss = " + this.realizedProfitLoss + "\n" ) ;
        builder.append( "   lastUpdate = " + this.lastUpdate + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}