package com.sandy.capitalyst.server.api.equity.helper;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

public class EquityHoldingVO extends EquityHolding {
    
    private float valueAtCost = 0 ;
    private float valueAtMktPrice = 0 ;
    
    private int ltcgQty = 0 ;
    private float taxAmount = 0 ;
    
    private float valuePostTax = 0 ;
    private float profitPostTax = 0 ;
    private float profitPctPostTax = 0 ;

    public EquityHoldingVO( EquityHolding holding ) {
        super( holding ) ;
        initializeDerivedValues() ;
    }

    private void initializeDerivedValues() {
        this.valueAtCost = (int)(super.getAvgCostPrice() * super.getQuantity()) ;
        this.valueAtMktPrice = (int)(super.getCurrentMktPrice() * super.getQuantity()) ;
    }
    
    public float getValueAtCost() {
        return valueAtCost ;
    }

    public void setValueAtCost( float valueAtCost ) {
        this.valueAtCost = valueAtCost ;
    }

    public float getValueAtMktPrice() {
        return valueAtMktPrice ;
    }

    public void setValueAtMktPrice( float valueAtMktPrice ) {
        this.valueAtMktPrice = valueAtMktPrice ;
    }

    public int getLtcgQty() {
        return ltcgQty ;
    }

    public void setLtcgQty( int ltcgQty ) {
        this.ltcgQty = ltcgQty ;
    }

    public float getTaxAmount() {
        return taxAmount ;
    }

    public void setTaxAmount( float taxAmount ) {
        this.taxAmount = taxAmount ;
    }

    public float getValuePostTax() {
        return valuePostTax ;
    }

    public void setValuePostTax( float valuePostTax ) {
        this.valuePostTax = valuePostTax ;
    }

    public float getProfitPostTax() {
        return profitPostTax ;
    }

    public void setProfitPostTax( float profitPostTax ) {
        this.profitPostTax = profitPostTax ;
    }

    public float getProfitPctPostTax() {
        return profitPctPostTax ;
    }

    public void setProfitPctPostTax( float profitPctPostTax ) {
        this.profitPctPostTax = profitPctPostTax ;
    }
}
