package com.sandy.capitalyst.server.dao.mf;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "mf_portfolio" )
public class MutualFundAsset {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String ownerName = null ;
    private String scheme = null ;
    private String isin = null ;
    private String category = null ;
    private String subCategory = null ;
    private float unitsHeld = 0.0f ;
    private float avgCostPrice = 0.0f ;
    private float valueAtCost = 0.0f ;
    private float lastRecordedNav = 0.0f ;
    private float valueAtNav = 0.0f ;
    private float profitLossAmt = 0.0f ;
    private float profitLossPct = 0.0f ;
    private String url = null ;

    public MutualFundAsset() {}
    
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

    public void setScheme( String val ) {
        this.scheme = val ;
    }
        
    public String getScheme() {
        return this.scheme ;
    }

    public void setIsin( String val ) {
        this.isin = val ;
    }
        
    public String getIsin() {
        return this.isin ;
    }

    public void setCategory( String val ) {
        this.category = val ;
    }
        
    public String getCategory() {
        return this.category ;
    }

    public void setSubCategory( String val ) {
        this.subCategory = val ;
    }
        
    public String getSubCategory() {
        return this.subCategory ;
    }

    public void setUnitsHeld( float val ) {
        this.unitsHeld = val ;
    }
        
    public float getUnitsHeld() {
        return this.unitsHeld ;
    }

    public void setAvgCostPrice( float val ) {
        this.avgCostPrice = val ;
    }
        
    public float getAvgCostPrice() {
        return this.avgCostPrice ;
    }

    public void setValueAtCost( float val ) {
        this.valueAtCost = val ;
    }
        
    public float getValueAtCost() {
        return this.valueAtCost ;
    }

    public void setLastRecordedNav( float val ) {
        this.lastRecordedNav = val ;
    }
        
    public float getLastRecordedNav() {
        return this.lastRecordedNav ;
    }

    public void setValueAtNav( float val ) {
        this.valueAtNav = val ;
    }
        
    public float getValueAtNav() {
        return this.valueAtNav ;
    }

    public void setProfitLossAmt( float val ) {
        this.profitLossAmt = val ;
    }
        
    public float getProfitLossAmt() {
        return this.profitLossAmt ;
    }

    public void setProfitLossPct( float val ) {
        this.profitLossPct = val ;
    }
        
    public float getProfitLossPct() {
        return this.profitLossPct ;
    }
    
    public String getUrl() {
        return url ;
    }

    public void setUrl( String url ) {
        this.url = url ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "MutualFundAsset [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   ownerName = " + this.ownerName + "\n" ) ;
        builder.append( "   scheme = " + this.scheme + "\n" ) ;
        builder.append( "   isin = " + this.isin + "\n" ) ;
        builder.append( "   category = " + this.category + "\n" ) ;
        builder.append( "   subCategory = " + this.subCategory + "\n" ) ;
        builder.append( "   unitsHeld = " + this.unitsHeld + "\n" ) ;
        builder.append( "   avgCostPrice = " + this.avgCostPrice + "\n" ) ;
        builder.append( "   valueAtCost = " + this.valueAtCost + "\n" ) ;
        builder.append( "   lastRecordedNav = " + this.lastRecordedNav + "\n" ) ;
        builder.append( "   valueAtNav = " + this.valueAtNav + "\n" ) ;
        builder.append( "   profitLossAmt = " + this.profitLossAmt + "\n" ) ;
        builder.append( "   profitLossPct = " + this.profitLossPct + "\n" ) ;
        builder.append( "   url = " + this.url + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
