package com.sandy.capitalyst.server.api.equity.vo;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = false)
public class IndividualEquityHoldingVO extends EquityHolding {
    
    private float  valueAtCost     = 0 ;
    private float  valueAtMktPrice = 0 ;
    private int    ltcgQty         = 0 ;
    private float  taxAmount       = 0 ;
    private float  sellBrokerage   = 0 ;
    private float  valuePostTax    = 0 ;
    private float  pat             = 0 ; // Profit after tax
    private float  patPct          = 0 ; // Profit after tax percentage
    private String detailUrl       = null ;
    
    private List<EquityBuyTxnVO> buyTxnVOList = new ArrayList<>() ;
    
    private List<Integer> sparklineData = null ;
    
    private EquityIndicators indicators = null ;

    public IndividualEquityHoldingVO( EquityHolding holding ) {
        super( holding ) ;
        initializeDerivedValues() ;
    }

    private void initializeDerivedValues() {
        this.valueAtCost = (int)(super.getAvgCostPrice() * super.getQuantity()) ;
        this.valueAtMktPrice = (int)(super.getCurrentMktPrice() * super.getQuantity()) ;
    }

    public void addEquityBuyTxnVO( EquityBuyTxnVO txnVO ) {
        
        EquityBuyTxnVO dayAggTxn = getDayAggregateTxn( txnVO ) ;
        if( dayAggTxn == null ) {
            dayAggTxn = new EquityBuyTxnVO( txnVO ) ;
            this.buyTxnVOList.add( dayAggTxn ) ;
        }
        else {
            dayAggTxn.aggregate( txnVO ) ;
        }
    }
    
    private EquityBuyTxnVO getDayAggregateTxn( EquityBuyTxnVO vo ) {
        
        String voOwner = vo.getHolding().getOwnerName() ;
        String voScrip = vo.getHolding().getSymbolNse() ;
        
        for( EquityBuyTxnVO txn : buyTxnVOList ) {
            
            String txnOwner = txn.getHolding().getOwnerName() ;
            String txnScrip = txn.getHolding().getSymbolNse() ;
            
            if( voOwner.equals( txnOwner ) ) {
                if( vo.getAction().equals( txn.getAction() ) ) {
                    if( vo.getTxnDate().equals( txn.getTxnDate() ) ) {
                        if( voScrip.equals( txnScrip ) ) {
                            return txn ;
                        }
                    }
                }
            }
        }
        return null ;
    }
    
    public void computeTaxOnSell() {
        
        if( getQuantity() <= 0 ) {
            return ;
        }
        
        int stcgQty = super.getQuantity() - ltcgQty ;
        
        float ltcgCost = 0, ltcgValue = 0, ltcgProfit = 0, ltcgTax = 0 ;
        float stcgCost = 0, stcgValue = 0, stcgProfit = 0, stcgTax = 0 ;
        
        if( super.getOwnerName().equals( "Sandeep" ) ) {
            sellBrokerage = (float)( valueAtMktPrice * (0.24/100)) ;
        }
        else {
            sellBrokerage = (float)( valueAtMktPrice * (0.77/100)) ;
        }
        
        ltcgCost   = ltcgQty * super.getAvgCostPrice() ;
        ltcgValue  = ltcgQty * super.getCurrentMktPrice() ;
        ltcgProfit = ltcgValue - ltcgCost ;
        ltcgTax    = ltcgProfit > 0 ? 0.1f * ltcgProfit : 0 ;
        
        stcgCost   = stcgQty * super.getAvgCostPrice() ;
        stcgValue  = stcgQty * super.getCurrentMktPrice() ;
        stcgProfit = stcgValue - stcgCost ;
        stcgTax    = stcgProfit > 0 ? 0.3f * stcgProfit : 0 ;
        
        taxAmount    = ltcgTax + stcgTax ;
        valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
        pat          = valuePostTax - valueAtCost ;
        patPct       = ( pat / valueAtCost ) * 100 ; 
        
        computeSellTaxForIndividualTxns() ;
    }
    
    public void computeSellTaxForIndividualTxns() {
        for( EquityBuyTxnVO vo : buyTxnVOList ) {
            vo.computeSellTax() ;
        }
    }
}
