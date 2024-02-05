package com.sandy.capitalyst.server.api.equity.vo;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.core.util.BrokerageUtil.computeBrokerage ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.fasterxml.jackson.annotation.JsonIgnore ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false )
public class EquitySellTxnVO extends EquityTxn {
    
    static final Logger log = Logger.getLogger( EquitySellTxnVO.class ) ;
    
    private EquityHolding parentHolding = null ;
    private boolean isETF = false ;
    
    @JsonIgnore
    private List<AssociatedBuyTxn> buyTxns = new ArrayList<>() ;

    float valueAtCostPrice = 0 ;
    float valueAtMktPrice  = 0 ;
    float profit           = 0 ;
    float taxAmount        = 0 ;
    float valuePostTax     = 0 ;
    float sellTxnCharges   = 0 ;
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
        float pat              = 0 ;
        float patPct           = 0 ;
        
        float sellBrokerage           = 0 ;
        float sellExchangeTxnCharges  = 0 ;
        float sellSEBITurnoverCharges = 0 ;
        float sellGST                 = 0 ;
        float sellSTT                 = 0 ;
        float sellTotalTxnCharges     = 0 ;
        
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
                                 ( ((float)redeemedQty) / ((float)buyTxn.getQuantity()) ) ;
            
            valueAtCostPrice = ( redeemedQty * buyTxn.getTxnPrice() ) + overhead ;
            valueAtMktPrice  = redeemedQty * sellTxnPrice ;
            
            computeSellTxnCharges() ;
            computeSellTax() ;
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
        
        private void computeSellTax() {
            
            profit = valueAtMktPrice - valueAtCostPrice - sellTotalTxnCharges ;
            if( profit > 0 ) {
                boolean ltcgQualified = qualifiesForLTCG() ;
                taxAmount = ltcgQualified ? 0.1f * profit : 
                                            0.15f * profit ;
            }
            
            float surcharge = taxAmount * 0.15f ;
            float cess = (taxAmount + surcharge) * 0.04f ;
            
            taxAmount += ( surcharge + cess ) ;
            
            valuePostTax = valueAtMktPrice - taxAmount - sellTotalTxnCharges ;
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
        this.parentHolding = vo.getParentHolding()  ;
        setETFFlag( this.parentHolding.getIsin() ) ;
    }

    public EquitySellTxnVO( EquityHolding holding, EquityTxn sellTxn ) {
        
        super( sellTxn ) ;
        this.parentHolding = holding ;
        setETFFlag( this.parentHolding.getIsin() ) ;
    }
    
    private void setETFFlag( String isin ) {
        
        EquityMasterRepo emRepo = getBean( EquityMasterRepo.class ) ; 
        EquityMaster em = emRepo.findByIsin( isin ) ;
        this.isETF = em.isEtf() ;
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
        
        this.valueAtCostPrice += abt.valueAtCostPrice    ;
        this.valueAtMktPrice  += abt.valueAtMktPrice     ;
        this.profit           += abt.profit              ;
        this.taxAmount        += abt.taxAmount           ;
        this.valuePostTax     += abt.valuePostTax        ;
        this.sellTxnCharges   += abt.sellTotalTxnCharges ;
        
        this.pat    = this.valuePostTax - this.valueAtCostPrice ;
        this.patPct = ( this.pat / this.valueAtCostPrice ) * 100 ; 
    }
}
