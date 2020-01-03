package com.sandy.capitalyst.server.api.mf;

public class MFHolding {

    private String ownerName = null ;
    private String isin = null ;
    private String scheme = null ;
    private String category = null ;
    private String subCategory = null ;
    
    private float unitsHeld = 0 ;
    private float valueAtCost = 0 ;
    private float valueAtNav = 0 ;
    
    private float ltcgValueAtNavAfterTax = 0 ;
    private float ltcgProfitLossAmtAfterTax = 0 ;
    private float ltcgProfitLossPctAfterTax = 0 ;
    
    private float valueAtNavAfterTax = 0 ;
    private float profitLossAmtAfterTax = 0 ;
    private float profitLossAmtPctAfterTax = 0 ;
    
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
    public void setUnitsHeld( float unitsHeld ) {
        this.unitsHeld = unitsHeld ;
    }
    
    public float getValueAtCost() {
        return valueAtCost ;
    }
    public void setValueAtCost( float valueAtCost ) {
        this.valueAtCost = valueAtCost ;
    }
    
    public float getValueAtNav() {
        return valueAtNav ;
    }
    public void setValueAtNav( float valueAtNav ) {
        this.valueAtNav = valueAtNav ;
    }
    
    public float getLtcgValueAtNavAfterTax() {
        return ltcgValueAtNavAfterTax ;
    }
    public void setLtcgValueAtNavAfterTax( float value ) {
        this.ltcgValueAtNavAfterTax = value ;
    }
    
    public float getLtcgProfitLossAmtAfterTax() {
        return ltcgProfitLossAmtAfterTax ;
    }
    public void setLtcgProfitLossAmtAfterTax( float value ) {
        this.ltcgProfitLossAmtAfterTax = value ;
    }
    
    public float getLtcgProfitLossPctAfterTax() {
        return ltcgProfitLossPctAfterTax ;
    }
    public void setLtcgProfitLossPctAfterTax( float value ) {
        this.ltcgProfitLossPctAfterTax = value ;
    }
    
    public float getValueAtNavAfterTax() {
        return valueAtNavAfterTax ;
    }
    public void setValueAtNavAfterTax( float value ) {
        this.valueAtNavAfterTax = value ;
    }
    
    public float getProfitLossAmtAfterTax() {
        return profitLossAmtAfterTax ;
    }
    public void setProfitLossAmtAfterTax( float value ) {
        this.profitLossAmtAfterTax = value ;
    }
    
    public float getProfitLossAmtPctAfterTax() {
        return profitLossAmtPctAfterTax ;
    }
    public void setProfitLossAmtPctAfterTax( float value ) {
        this.profitLossAmtPctAfterTax = value ;
    }
    
    @Override
    public String toString() {
        return "MFHolding ["
                + "\n  ownerName=" + ownerName 
                + "\n  isin=" + isin
                + "\n  scheme=" + scheme 
                + "\n  category=" + category
                + "\n  subCategory=" + subCategory 
                + "\n  unitsHeld=" + unitsHeld
                + "\n  valueAtCost=" + valueAtCost 
                + "\n  valueAtNav=" + valueAtNav
                + "\n  ltcgValueAtNavAfterTax=" + ltcgValueAtNavAfterTax
                + "\n  ltcgProfitLossAmtAfterTax=" + ltcgProfitLossAmtAfterTax
                + "\n  ltcgProfitLossPctAfterTax=" + ltcgProfitLossPctAfterTax
                + "\n  valueAtNavAfterTax=" + valueAtNavAfterTax
                + "\n  profitLossAmtAfterTax=" + profitLossAmtAfterTax
                + "\n  profitLossAmtPctAfterTax=" + profitLossAmtPctAfterTax
                + "]" ;
    }
}
