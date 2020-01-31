package com.sandy.capitalyst.server.api.equity.helper;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

class EquityLot {
    
    int numUnitsLeft = 0 ;
    Date txnDate = null ;
    
    public EquityLot( int units, Date date ) {
        this.numUnitsLeft = units ;
        this.txnDate = date ;
    }
    
    // Returns the number of units that COULD NOT be redeemed
    public int redeemUnits( int unitsToBeRedeemed ) {
        int redeemedUnits = 0 ;
        if( numUnitsLeft > 0 ) {
            if( unitsToBeRedeemed <= numUnitsLeft ) {
                redeemedUnits = unitsToBeRedeemed ;
            }
            else {
                redeemedUnits = numUnitsLeft ;
            }
        }
        numUnitsLeft -= redeemedUnits ;
        
        int unRedeemedUnits = unitsToBeRedeemed - redeemedUnits ;
        return unRedeemedUnits ;
    }
    
    public int getTenureInDays() {
        long numMillis = new Date().getTime() - txnDate.getTime() ;
        return (int)( numMillis / (1000*60*60*24) ) ;
    }
    
    public boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return txnDate.before( oneYearPastDate ) ;
    }
}

public class EquityHoldingVOBuilder {

    public EquityHoldingVO buildVO( EquityHolding holding, List<EquityTxn> txns ) {
        
        EquityHoldingVO vo = new EquityHoldingVO( holding ) ;
        List<EquityLot> lots = processTxns( txns ) ;
        
        vo.setLtcgQty( computeLTCGQty( lots ) ) ;
        if( vo.getQuantity() > 0 ) {
            computeTax( vo ) ;
        }
        return vo ;
    }
    
    private int computeLTCGQty( List<EquityLot> lots ) {
        
        int ltcgQty = 0 ;
        for( EquityLot lot : lots ) {
            if( lot.numUnitsLeft > 0 ) {
                if( lot.qualifiesForLTCG() ) {
                    ltcgQty += lot.numUnitsLeft ;
                }
            }
        }
        return ltcgQty ;
    }
    
    private void computeTax( EquityHoldingVO vo ) {
        
        int ltcgQty = vo.getLtcgQty() ;
        int stcgQty = vo.getQuantity() - ltcgQty ;
        
        float ltcgCost = 0, ltcgValue = 0, ltcgProfit = 0, ltcgTax = 0 ;
        float stcgCost = 0, stcgValue = 0, stcgProfit = 0, stcgTax = 0 ;
        float totalTax = 0 ;
        float valuePostTax = 0 ;
        float profitPostTax = 0 ;
        float profitPctPostTax = 0 ;
        
        ltcgCost = ltcgQty * vo.getAvgCostPrice() ;
        ltcgValue = ltcgQty * vo.getCurrentMktPrice() ;
        ltcgProfit = ltcgValue - ltcgCost ;
        ltcgTax = ltcgProfit > 0 ? 0.1f * ltcgProfit : 0 ;
        
        stcgCost = stcgQty * vo.getAvgCostPrice() ;
        stcgValue = stcgQty * vo.getCurrentMktPrice() ;
        stcgProfit = stcgValue - stcgCost ;
        stcgTax = stcgProfit > 0 ? 0.3f * stcgProfit : 0 ;
        
        totalTax = ltcgTax + stcgTax ;
        valuePostTax = vo.getValueAtMktPrice() - totalTax ;
        profitPostTax = valuePostTax - vo.getValueAtCost() ;
        profitPctPostTax = ( profitPostTax / vo.getValueAtCost() ) * 100 ; 
        
        vo.setValuePostTax( valuePostTax ) ;
        vo.setProfitPostTax( profitPostTax ) ;
        vo.setProfitPctPostTax( profitPctPostTax ) ;
    }
    
    private List<EquityLot> processTxns( List<EquityTxn> txns ) {
        
        List<EquityLot> lots = new ArrayList<>() ;
        
        if( txns != null && !txns.isEmpty() ) {
            for( EquityTxn txn : txns ) {
                String action = txn.getAction() ;
                
                if( action.equalsIgnoreCase( "buy" ) ) {
                    lots.add( new EquityLot( txn.getQuantity(), txn.getTxnDate() ) ) ;
                }
                else if( action.equalsIgnoreCase( "sell" ) ) {
                    
                    int unRedeemedUnits = txn.getQuantity() ;
                    Iterator<EquityLot> allLotsIter = lots.iterator() ;
                    while( unRedeemedUnits > 0 && allLotsIter.hasNext() ) {
                        EquityLot lot = allLotsIter.next() ;
                        unRedeemedUnits = lot.redeemUnits( unRedeemedUnits ) ;
                    }
                }
                else {
                    throw new RuntimeException( "Unknown equity txn action - " + action ) ;
                }
            }
        }
        return lots ;
    }
}
