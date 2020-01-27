package com.sandy.capitalyst.server.api.equity.helper;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

public class EquityHoldingVO extends EquityHolding {
    
    private float valueAtCost = 0 ;
    private float valueAtMktPrice = 0 ;

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
}
