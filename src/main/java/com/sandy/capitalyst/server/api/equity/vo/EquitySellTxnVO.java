package com.sandy.capitalyst.server.api.equity.vo;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false )
public class EquitySellTxnVO extends EquityTxn {
    
    private EquityHolding holding = null ;
    
    private List<AssociatedBuyTxn> buyTxns = new ArrayList<>() ;

    float valueAtCostPrice = 0 ;
    float valueAtMktPrice  = 0 ;
    float profit           = 0 ;
    float taxAmount        = 0 ;
    float valuePostTax     = 0 ;
    float sellBrokerage    = 0 ;
    float pat              = 0 ;
    float patPct           = 0 ;

    @Data
    private class AssociatedBuyTxn {
        
        private int redeemedQty = 0 ;
        private EquityTxn buyTxn = null ;
        
        float valueAtCostPrice = 0 ;
        float valueAtMktPrice  = 0 ;
        float profit           = 0 ;
        float taxAmount        = 0 ;
        float valuePostTax     = 0 ;
        float sellBrokerage    = 0 ;
        float pat              = 0 ;
        float patPct           = 0 ;
        
        public AssociatedBuyTxn( EquityTxn buyTxn, int redeemedQty ) {
            this.buyTxn = buyTxn ;
            this.redeemedQty = redeemedQty ;
            
            computeDerivedValues() ;
        }
        
        private void computeDerivedValues() {
            
            float sellTxnPrice = getTxnPrice() ;
            float overhead     = ( buyTxn.getBrokerage() + 
                                   buyTxn.getStampDuty() + 
                                   buyTxn.getTxnCharges() ) * 
                                 ( redeemedQty / buyTxn.getQuantity() ) ;
            
            valueAtCostPrice = ( redeemedQty * buyTxn.getTxnPrice() ) + overhead ;
            valueAtMktPrice  = redeemedQty * sellTxnPrice ;
            
            computeSellTax() ;
        }
        
        public void computeSellTax() {
            
            if( holding.getOwnerName().equals( "Sandeep" ) ) {
                sellBrokerage = (float)( valueAtMktPrice * (0.24/100)) ;
            }
            else {
                sellBrokerage = (float)( valueAtMktPrice * (0.77/100)) ;
            }
            
            profit = valueAtMktPrice - valueAtCostPrice ;
            if( profit > 0 ) {
                boolean ltcgQualified = qualifiesForLTCG() ;
                taxAmount = ltcgQualified ? 0.1f * profit : 0.3f * profit ;
            }
            
            valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
            pat          = valuePostTax - valueAtCostPrice ;
            patPct       = ( pat / valueAtCostPrice ) * 100 ; 
        }
        
        private boolean qualifiesForLTCG() {
            Date oneYearPastDate = DateUtils.addYears( getTxnDate(), -1 ) ;
            return buyTxn.getTxnDate().before( oneYearPastDate ) ;
        }
    }
    
    public EquitySellTxnVO( EquitySellTxnVO vo ) {
        super( vo ) ;
        this.holding = vo.getHolding()  ;
    }

    public EquitySellTxnVO( IndividualEquityHoldingVO holding, 
                            EquityTxn sellTxn ) {
        
        super( sellTxn ) ;
        this.holding = holding.getBaseHolding() ;
    }
    
    // If there is another sell transaction on the same date, this method will
    // aggregate the given txn into this transaction. This is a way to merge
    // multiple sell transactions on a day into one transaction.
    public void aggregate( EquitySellTxnVO txnVO ) {
        
        if( !txnVO.getTxnDate().equals( this.getTxnDate() ) ) {
            throw new IllegalArgumentException( "Txn date not same." ) ;
        }
        
        float totalSellPrice = ( super.getQuantity() * super.getTxnPrice() ) +
                               ( txnVO.getQuantity() * txnVO.getTxnPrice() ) ;
        
        super.setQuantity  ( super.getQuantity()   + txnVO.getQuantity()   ) ;
        super.setBrokerage ( super.getBrokerage()  + txnVO.getBrokerage()  ) ;
        super.setTxnCharges( super.getTxnCharges() + txnVO.getTxnCharges() ) ;
        super.setStampDuty ( super.getStampDuty()  + txnVO.getStampDuty()  ) ;
        super.setTxnPrice  ( totalSellPrice / super.getQuantity() ) ;
    }

    public void associateBuyTxn( EquityTxn buyTxn, int redeemQty ) {
        
        AssociatedBuyTxn abt = new AssociatedBuyTxn( buyTxn, redeemQty ) ;
        buyTxns.add( abt ) ;
        
        this.valueAtCostPrice += abt.valueAtCostPrice ;
        this.valueAtMktPrice  += abt.valueAtMktPrice  ;
        this.profit           += abt.profit           ;
        this.taxAmount        += abt.taxAmount        ;
        this.valuePostTax     += abt.valuePostTax     ;
        this.sellBrokerage    += abt.sellBrokerage    ;
        
        this.pat    = this.valuePostTax - this.valueAtCostPrice ;
        this.patPct = ( this.pat / this.valueAtCostPrice ) * 100 ; 
    }
}
