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
    
    private int   quantityLeft     = 0 ;
    
    private float valueAtCost      = 0 ;
    private float valueAtMktPrice  = 0 ;
    private float taxAmount        = 0 ;
    private float sellBrokerage    = 0 ;
    private float valuePostTax     = 0 ;
    private float pat              = 0 ; // Profit after tax
    private float patPct           = 0 ; // Profit after tax percentage
    
    public EquityTxnVO( IndividualEquityHoldingVO holding, EquityTxn txn ) {
        super( txn ) ;
        this.holding = holding ;
        this.quantityLeft = txn.getQuantity() ;
        
        computeDerivedValues() ;
    }

    public void redeemQuantity( int redeemedQty ) {
        this.quantityLeft -= redeemedQty ;
        computeDerivedValues() ;
    }
    
    public void computeTax() {
        
        boolean ltcg = qualifiesForLTCG() ;
        
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
            taxAmount = ltcg ? 0.1f * profit : 0.3f * profit ;
        }
        
        valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
        pat          = valuePostTax - valueAtCost ;
        patPct       = ( pat / valueAtCost ) * 100 ; 
    }

    private void computeDerivedValues() {
        this.valueAtCost = (int)( super.getTxnPrice() * quantityLeft ) ;
        this.valueAtMktPrice = (int)( holding.getCurrentMktPrice() * quantityLeft ) ;
    }
    
    private boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return super.getTxnDate().before( oneYearPastDate ) ;
    }
}
