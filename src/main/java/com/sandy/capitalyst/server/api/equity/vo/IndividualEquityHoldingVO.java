package com.sandy.capitalyst.server.api.equity.vo;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.core.util.BrokerageUtil.computeBrokerage ;

import java.util.ArrayList ;
import java.util.List ;

import com.fasterxml.jackson.annotation.JsonIgnore ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode(callSuper = false)
public class IndividualEquityHoldingVO extends EquityHolding {
    
    private boolean isETF = false ;
    
    private float  valueAtCost     = 0 ;
    private float  valueAtMktPrice = 0 ;
    private int    ltcgQty         = 0 ;
    private float  taxAmount       = 0 ;
    private float  valuePostTax    = 0 ;
    private float  pat             = 0 ; // Profit after tax
    private float  patPct          = 0 ; // Profit after tax percentage
    private String detailUrl       = null ;
    
    private float sellBrokerage           = 0 ;
    private float sellExchangeTxnCharges  = 0 ;
    private float sellSEBITurnoverCharges = 0 ;
    private float sellGST                 = 0 ;
    private float sellSTT                 = 0 ;
    private float sellTotalTxnCharges     = 0 ;
    
    private String uniqueId         = null ;
    private String holdingType      = "Individual" ;
    
    private List<EquityBuyTxnVO> buyTxnVOList = new ArrayList<>() ;
    
    private List<Integer> sparklineData = null ;
    
    private EquityIndicators indicators = null ;
    
    @JsonIgnore
    private EquityHolding parentHolding = null ;

    public IndividualEquityHoldingVO( EquityHolding holding ) {
        super( holding ) ;
        this.parentHolding = holding ;
        initializeDerivedValues() ;
    }

    private void initializeDerivedValues() {
        this.valueAtCost = (int)(super.getAvgCostPrice() * super.getQuantity()) ;
        this.valueAtMktPrice = (int)(super.getCurrentMktPrice() * super.getQuantity()) ;
        
        EquityMasterRepo emRepo = getBean( EquityMasterRepo.class ) ; 
        EquityMaster em = emRepo.findByIsin( super.getIsin() ) ;
        this.isETF = em.isEtf() ;
        this.uniqueId = "IndHolding-" + getOwnerName() + "-" + getIsin() ;
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
        
        String voOwner = vo.getParentHolding().getOwnerName() ;
        String voScrip = vo.getParentHolding().getSymbolNse() ;
        
        for( EquityBuyTxnVO txn : buyTxnVOList ) {
            
            String txnOwner = txn.getParentHolding().getOwnerName() ;
            String txnScrip = txn.getParentHolding().getSymbolNse() ;
            
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
    
    private void computeSellTxnCharges() {
        
        sellBrokerage = computeBrokerage( valueAtMktPrice, 
                                          parentHolding.getOwnerName() ) ;

        // Exchange transaction charges are at 0.0032% of the transaction value
        sellExchangeTxnCharges = (float)( valueAtMktPrice * (0.0032/100) ) ;
        
        // SEBI turnover charges at 0.0001% of the transaction value
        sellSEBITurnoverCharges = (float)( valueAtMktPrice * (0.0001/100) ) ;
        
        // GST is 18% of the sum of brokerage, exchange txn chgs and SEBI turnover charges
        sellGST = ( sellBrokerage + sellExchangeTxnCharges + sellSEBITurnoverCharges ) *
                  ( 18F/100F ) ;
        
        // STT is 0.001% if the stock is a ETF, else it is 0.1% of the 
        // transaction value
        if( isETF ) {
            sellSTT = (float)( valueAtMktPrice * (0.001/100) ) ;
        }
        else {
            sellSTT = (float)( valueAtMktPrice * (0.1/100) ) ;
        }
        
        sellTotalTxnCharges = sellBrokerage + 
                              sellExchangeTxnCharges + 
                              sellSEBITurnoverCharges + 
                              sellGST + 
                              sellSTT ;
    }
    
    public void computeTaxOnSell() {
        
        if( getQuantity() <= 0 ) {
            return ;
        }
        
        computeSellTxnCharges() ;
        
        int stcgQty = super.getQuantity() - ltcgQty ;
        
        float ltcgCost = 0, ltcgValue = 0, ltcgProfit = 0, ltcgTax = 0 ;
        float stcgCost = 0, stcgValue = 0, stcgProfit = 0, stcgTax = 0 ;
        
        ltcgCost   = ltcgQty * super.getAvgCostPrice() ;
        ltcgValue  = ltcgQty * super.getCurrentMktPrice() ;
        ltcgProfit = ltcgValue - ltcgCost ;
        ltcgTax    = ltcgProfit > 0 ? 0.1f * ltcgProfit : 0 ;
        
        stcgCost   = stcgQty * super.getAvgCostPrice() ;
        stcgValue  = stcgQty * super.getCurrentMktPrice() ;
        stcgProfit = stcgValue - stcgCost ;
        stcgTax    = stcgProfit > 0 ? 0.3f * stcgProfit : 0 ;
        
        taxAmount    = ltcgTax + stcgTax ;
        valuePostTax = valueAtMktPrice - taxAmount - sellTotalTxnCharges ;
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
