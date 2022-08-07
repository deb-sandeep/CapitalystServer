package com.sandy.capitalyst.server.api.equity.vo;

import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;

import com.fasterxml.jackson.annotation.JsonIgnore ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false )
public class EquityTxnVO extends EquityTxn {

    @JsonIgnore
    private IndividualEquityHoldingVO holding = null ;
    
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
    
    // LTCG qualified
    
    public EquityTxnVO( IndividualEquityHoldingVO holding, EquityTxn txn ) {
        super( txn ) ;
        this.holding = holding ;
        this.ltcgQuailifed = qualifiesForLTCG() ;
        this.quantityLeft = txn.getQuantity() ;
        
        computeDerivedValues() ;
    }
    
    public EquityTxnVO( EquityTxnVO vo ) {
        super( vo ) ;
        this.holding         = vo.holding ;
        this.quantityLeft    = vo.quantityLeft ;
        this.ltcgQuailifed   = vo.ltcgQuailifed ;
        this.valueAtCost     = vo.valueAtCost ;
        this.valueAtMktPrice = vo.valueAtMktPrice ;
        this.taxAmount       = vo.taxAmount ;
        this.sellBrokerage   = vo.sellBrokerage ;
        this.valuePostTax    = vo.valuePostTax ;
        this.pat             = vo.pat ;
        this.patPct          = vo.patPct ;
        this.durationInMonths= vo.durationInMonths ;
    }

    public void redeemQuantity( int redeemedQty ) {
        this.quantityLeft -= redeemedQty ;
        computeDerivedValues() ;
    }
    
    public void computeTax() {
        
        float cost = 0, value = 0, profit = 0 ;
        
        if( holding.getOwnerName().equals( "Sandeep" ) ) {
            sellBrokerage = (float)( valueAtMktPrice * (0.24/100)) ;
        }
        else {
            sellBrokerage = (float)( valueAtMktPrice * (0.77/100)) ;
        }
        
        cost = ( quantityLeft * super.getTxnPrice() ) + 
               super.getBrokerage() + 
               super.getTxnCharges() + 
               super.getStampDuty() ;
        
        value  = quantityLeft * holding.getCurrentMktPrice() ;
        profit = value - cost ;
        if( profit > 0 ) {
            taxAmount = ltcgQuailifed ? 0.1f * profit : 0.3f * profit ;
        }
        
        valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
        pat          = valuePostTax - valueAtCost ;
        patPct       = ( pat / valueAtCost ) * 100 ; 
    }

    private void computeDerivedValues() {
        this.valueAtCost = (int)((super.getTxnPrice() * quantityLeft) +
                                  super.getBrokerage() + 
                                  super.getTxnCharges() + 
                                  super.getStampDuty() ) ;
        this.valueAtMktPrice = (int)( holding.getCurrentMktPrice() * quantityLeft ) ;
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
    
    private boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return super.getTxnDate().before( oneYearPastDate ) ;
    }

    public void aggregate( EquityTxnVO txnVO ) {
        
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
