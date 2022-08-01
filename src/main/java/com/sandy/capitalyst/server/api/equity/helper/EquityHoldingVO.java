package com.sandy.capitalyst.server.api.equity.helper;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = false)
public class EquityHoldingVO extends EquityHolding {
    
    private float valueAtCost = 0 ;
    private float valueAtMktPrice = 0 ;
    
    private int ltcgQty = 0 ;
    private float taxAmount = 0 ;
    private float brokerageAmount = 0 ;
    
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
}
