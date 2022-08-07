package com.sandy.capitalyst.server.api.equity.vo;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = false)
public class FamilyEquityHoldingVO {
    
    // Static quantities
    private String symbolIcici      = null ;
    private String symbolNse        = null ;
    private String companyName      = null ;
    private String isin             = null ;
    private float  currentMktPrice  = 0.0f ;
    private Date   lastUpdate       = null ;
    
    // Aggregate quantities
    private int    quantity         = 0 ;
    private float  valueAtCost      = 0 ;
    private float  valueAtMktPrice  = 0 ;
    private int    ltcgQty          = 0 ;
    private float  taxAmount        = 0 ;
    private float  sellBrokerage    = 0 ;
    private float  dayGain          = 0 ;

    // Computed quantities
    private float  avgCostPrice     = 0.0f ;
    private float  valuePostTax     = 0 ;
    private float  pat              = 0 ; // Profit after tax
    private float  patPct           = 0 ; // Profit after tax percentage
    
    private List<IndividualEquityHoldingVO> holdings = new ArrayList<>() ;

    public FamilyEquityHoldingVO( IndividualEquityHoldingVO holding ) {
        
        this.symbolIcici     = holding.getSymbolIcici() ;
        this.symbolNse       = holding.getSymbolNse() ;
        this.companyName     = holding.getCompanyName() ;
        this.isin            = holding.getIsin() ;
        this.currentMktPrice = holding.getCurrentMktPrice() ;
        this.lastUpdate      = holding.getLastUpdate() ;
        
        addIndividualHoldingVO( holding ) ;
    }

    public void addIndividualHoldingVO( IndividualEquityHoldingVO holding ) {
        
        this.holdings.add( holding ) ;
        
        this.quantity        += holding.getQuantity() ;
        this.valueAtCost     += holding.getValueAtCost() ;
        this.valueAtMktPrice += holding.getValueAtMktPrice() ;
        this.ltcgQty         += holding.getLtcgQty() ;
        this.taxAmount       += holding.getTaxAmount() ;
        this.sellBrokerage   += holding.getSellBrokerage() ;
        this.dayGain         += holding.getDayGain() ;
        
        this.avgCostPrice     = this.valueAtCost / this.quantity ;
        this.valuePostTax     = this.valueAtMktPrice - taxAmount - sellBrokerage ;
        this.pat              = this.valuePostTax - this.valueAtCost ;
        this.patPct           = ( this.pat / this.valueAtCost )*100 ;
    }
}
