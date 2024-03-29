package com.sandy.capitalyst.server.api.equity.vo;

import static com.sandy.capitalyst.server.core.util.BrokerageUtil.computeBrokerage ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;

import com.fasterxml.jackson.annotation.JsonIgnore ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false )
public class EquityBuyTxnVO extends EquityTxn {

    @Data
    private class AssociatedSellTxn {
        
        private int redeemedQty = 0 ;
        private EquityTxn sellTxn = null ;
        
        public AssociatedSellTxn( EquityTxn sellTxn, int redeemedQty ) {
            this.sellTxn = sellTxn ;
            this.redeemedQty = redeemedQty ;
        }
    }
    
    @JsonIgnore
    private EquityTxn baseTxn = null ;
    
    private EquityHolding parentHolding = null ;
    
    private int     quantityLeft   = 0 ;
    private boolean ltcgQuailifed  = false ;
    
    private float valueAtCost      = 0 ;
    private float valueAtMktPrice  = 0 ;
    private float taxAmount        = 0 ;
    private float sellBrokerage    = 0 ;
    private float valuePostTax     = 0 ;
    private float pat              = 0 ; // Profit after tax
    private float patPct           = 0 ; // Profit after tax percentage
    private int   durationInMonths = 0 ;
    
    @JsonIgnore
    private List<AssociatedSellTxn> sellTxns = new ArrayList<>() ;
    
    public EquityBuyTxnVO( EquityBuyTxnVO txn ) {
        super( txn ) ;
        this.parentHolding   = txn.getParentHolding() ;
        this.quantityLeft    = txn.quantityLeft ;
        this.ltcgQuailifed   = txn.ltcgQuailifed ;
        this.valueAtCost     = txn.valueAtCost ;
        this.valueAtMktPrice = txn.valueAtMktPrice ;
        this.taxAmount       = txn.taxAmount ;
        this.sellBrokerage   = txn.sellBrokerage ;
        this.valuePostTax    = txn.valuePostTax ;
        this.pat             = txn.pat ;
        this.patPct          = txn.patPct ;
        this.durationInMonths= txn.durationInMonths ;
        
        this.sellTxns.clear() ;
        this.sellTxns.addAll( txn.getSellTxns() ) ;
        
        this.baseTxn = txn.getBaseTxn() ;
    }

    public EquityBuyTxnVO( EquityHolding holding, EquityTxn txn ) {
        
        super( txn ) ;
        
        if( !holding.getClass().getName().equals( EquityHolding.class.getName() ) ) {
            throw new IllegalStateException( "Wrong holding is being populated." ) ;
        }
        
        this.baseTxn = txn ;
        this.parentHolding = holding ;
        this.ltcgQuailifed = qualifiesForLTCG() ;
        this.quantityLeft = txn.getQuantity() ;
        
        computeDerivedValues() ;
        
    }
    
    private boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return super.getTxnDate().before( oneYearPastDate ) ;
    }

    private void computeDerivedValues() {
        this.valueAtCost = (int)((super.getTxnPrice() * quantityLeft) +
                                  super.getBrokerage() + 
                                  super.getTxnCharges() + 
                                  super.getStampDuty() ) ;
        this.valueAtMktPrice = (int)( parentHolding.getCurrentMktPrice() * quantityLeft ) ;
        this.durationInMonths = getDurationInMonths( super.getTxnDate() ) ;
    }
    
    private int getDurationInMonths( Date startDate ) {
        long now = new Date().getTime()/1000 ;
        long then = startDate.getTime()/1000 ;
        
        long seconds = now - then ;
        long hours   = seconds / 3600 ;
        long days    = hours / 24 ;
        int  months  = (int)days / 30 ;
        
        return months ;
    }
    
    public void redeemQuantity( EquityTxn sellTxn, int redeemedQty ) {
        this.quantityLeft -= redeemedQty ;
        this.sellTxns.add( new AssociatedSellTxn( sellTxn, redeemedQty ) ) ;
        computeDerivedValues() ;
    }
    
    public void computeSellTax() {
        
        float cost = 0, value = 0, profit = 0 ;
        
        sellBrokerage = computeBrokerage( valueAtMktPrice, 
                                          parentHolding.getOwnerName() ) ;

        cost = ( quantityLeft * super.getTxnPrice() ) + 
               super.getBrokerage() + 
               super.getTxnCharges() + 
               super.getStampDuty() ;
        
        value  = quantityLeft * parentHolding.getCurrentMktPrice() ;
        profit = value - cost ;
        if( profit > 0 ) {
            taxAmount = ltcgQuailifed ? 0.1f * profit : 0.15f * profit ;
            
            float surcharge = taxAmount * 0.15f ;
            float cess = (taxAmount + surcharge) * 0.04f ;
            
            taxAmount += ( surcharge + cess ) ;
        }
        
        valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
        pat          = valuePostTax - valueAtCost ;
        patPct       = ( pat / valueAtCost ) * 100 ; 
    }

    // If there is another buy transaction on the same date, this method will
    // aggregate the given txn into this transaction. This is a way to merge
    // multiple buy transactions on a day into one transaction.
    public void aggregate( EquityBuyTxnVO txnVO ) {
        
        if( !txnVO.getTxnDate().equals( this.getTxnDate() ) ) {
            throw new IllegalArgumentException( "Txn date not same." ) ;
        }
        
        super.setQuantity  ( super.getQuantity()   + txnVO.getQuantity()   ) ;
        super.setBrokerage ( super.getBrokerage()  + txnVO.getBrokerage()  ) ;
        super.setTxnCharges( super.getTxnCharges() + txnVO.getTxnCharges() ) ;
        super.setStampDuty ( super.getStampDuty()  + txnVO.getStampDuty()  ) ;
        
        this.quantityLeft    += txnVO.quantityLeft  ;
        this.taxAmount       += txnVO.taxAmount     ;
        this.sellBrokerage   += txnVO.sellBrokerage ;
        this.valuePostTax    += txnVO.valuePostTax  ;
        this.pat             += txnVO.pat           ; 
        this.valueAtCost     += txnVO.valueAtCost   ;
        this.valueAtMktPrice += txnVO.valueAtMktPrice ;
     
        super.setTxnPrice( this.getValueAtCost() / super.getQuantity() ) ;
        this.patPct = ( this.pat / this.valueAtCost )*100 ;
    }
}
