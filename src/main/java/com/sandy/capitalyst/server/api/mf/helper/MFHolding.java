package com.sandy.capitalyst.server.api.mf.helper;

public class MFHolding {

    private int assetId = -1 ;
    
    private String ownerName = null ;
    private String isin = null ;
    private String url = null ;
    private String scheme = null ;
    private String category = null ;
    private String subCategory = null ;
    private String purpose = null ;
    
    private int averageHoldingDays = 0 ;
    
    private float unitsHeld = 0 ;
    private float avgCostPrice = 0 ;
    private float valueAtCost = 0 ;
    private float currentNav = 0 ;
    private float valueAtNav = 0 ;
    
    private float numUnitsQualifiedForLTCG = 0 ;
    private float ltcgValueAtNavAfterTax = 0 ;
    private float ltcgProfitLossAmtAfterTax = 0 ;
    private float ltcgProfitLossPctAfterTax = 0 ;
    
    private float valueAtNavAfterTax = 0 ;
    private float profitLossAmtAfterTax = 0 ;
    private float profitLossAmtPctAfterTax = 0 ;
    
    public int getAssetId() {
        return assetId ;
    }
    public void setAssetId( int assetId ) {
        this.assetId = assetId ;
    }
    
    public String getOwnerName() {
        return ownerName ;
    }
    public void setOwnerName( String ownerName ) {
        this.ownerName = ownerName ;
    }
    
    public String getIsin() {
        return isin ;
    }
    public void setIsin( String isin ) {
        this.isin = isin ;
    }
    
    public String getScheme() {
        return scheme ;
    }
    public void setScheme( String scheme ) {
        this.scheme = scheme ;
    }
    
    public String getCategory() {
        return category ;
    }
    public void setCategory( String category ) {
        this.category = category ;
    }
    
    public String getSubCategory() {
        return subCategory ;
    }
    public void setSubCategory( String subCategory ) {
        this.subCategory = subCategory ;
    }
    
    public float getUnitsHeld() {
        return unitsHeld ;
    }
    public void setUnitsHeld( float value ) {
        this.unitsHeld = roundOff( value ) ;
    }
    
    public float getValueAtCost() {
        return valueAtCost ;
    }
    public void setValueAtCost( float value ) {
        this.valueAtCost = roundOff( value ) ;
    }
    
    public float getValueAtNav() {
        return valueAtNav ;
    }
    public void setValueAtNav( float value ) {
        this.valueAtNav = roundOff( value ) ;
    }
    
    public float getLtcgValueAtNavAfterTax() {
        return ltcgValueAtNavAfterTax ;
    }
    public void setLtcgValueAtNavAfterTax( float value ) {
        this.ltcgValueAtNavAfterTax = roundOff( value ) ;
    }
    
    public float getLtcgProfitLossAmtAfterTax() {
        return ltcgProfitLossAmtAfterTax ;
    }
    public void setLtcgProfitLossAmtAfterTax( float value ) {
        this.ltcgProfitLossAmtAfterTax = roundOff( value ) ;
    }
    
    public float getLtcgProfitLossPctAfterTax() {
        return ltcgProfitLossPctAfterTax ;
    }
    public void setLtcgProfitLossPctAfterTax( float value ) {
        this.ltcgProfitLossPctAfterTax = roundOff( value ) ;
    }
    
    public float getValueAtNavAfterTax() {
        return valueAtNavAfterTax ;
    }
    public void setValueAtNavAfterTax( float value ) {
        this.valueAtNavAfterTax = roundOff( value ) ;
    }
    
    public float getProfitLossAmtAfterTax() {
        return profitLossAmtAfterTax ;
    }
    public void setProfitLossAmtAfterTax( float value ) {
        this.profitLossAmtAfterTax = roundOff( value ) ;
    }
    
    public float getProfitLossAmtPctAfterTax() {
        return profitLossAmtPctAfterTax ;
    }
    public void setProfitLossAmtPctAfterTax( float value ) {
        this.profitLossAmtPctAfterTax = roundOff( value ) ;
    }
    
    public float getNumUnitsQualifiedForLTCG() {
        return numUnitsQualifiedForLTCG ;
    }
    public void setNumUnitsQualifiedForLTCG( float value ) {
        this.numUnitsQualifiedForLTCG = roundOff( value ) ;
    }
    
    public float getCurrentNav() {
        return currentNav ;
    }
    public void setCurrentNav( float value ) {
        this.currentNav = roundOff( value ) ;
    }
    
    public float getAvgCostPrice() {
        return avgCostPrice ;
    }
    public void setAvgCostPrice( float value ) {
        this.avgCostPrice = roundOff( value ) ;
    }
    
    public int getAverageHoldingDays() {
        return averageHoldingDays ;
    }
    public void setAverageHoldingDays( int value ) {
        this.averageHoldingDays = value ;
    }
    
    public String getUrl() {
        return url ;
    }
    public void setUrl( String url ) {
        this.url = url ;
    }
    
    public String getPurpose() {
        return purpose ;
    }
    public void setPurpose( String purpose ) {
        this.purpose = purpose ;
    }

    private float roundOff( float value ) {
        return (float)( Math.round( value * 100.0 ) / 100.0 ) ;
    }
    
    @Override
    public String toString() {
        return "MFHolding ["
                + "\n  assetId=" + assetId
                + "\n  ownerName=" + ownerName 
                + "\n  isin=" + isin
                + "\n  scheme=" + scheme 
                + "\n  category=" + category
                + "\n  subCategory=" + subCategory 
                + "\n  purpose=" + purpose
                + "\n  unitsHeld=" + unitsHeld
                + "\n  averageHoldingDays=" + averageHoldingDays
                + "\n  avgCostPrice=" + avgCostPrice
                + "\n  currentNav=" + currentNav
                + "\n  valueAtCost=" + valueAtCost 
                + "\n  valueAtNav=" + valueAtNav
                + "\n  numUnitsQualifiedForLTCG=" + numUnitsQualifiedForLTCG
                + "\n  ltcgValueAtNavAfterTax=" + ltcgValueAtNavAfterTax
                + "\n  ltcgProfitLossAmtAfterTax=" + ltcgProfitLossAmtAfterTax
                + "\n  ltcgProfitLossPctAfterTax=" + ltcgProfitLossPctAfterTax
                + "\n  valueAtNavAfterTax=" + valueAtNavAfterTax
                + "\n  profitLossAmtAfterTax=" + profitLossAmtAfterTax
                + "\n  profitLossAmtPctAfterTax=" + profitLossAmtPctAfterTax
                + "\n  url=" + url
                + "]" ;
    }
}
