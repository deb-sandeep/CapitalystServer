package com.sandy.capitalyst.server.api.equity.helper;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

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
        
        int unRedeemedUnits = unitsToBeRedeemed ;
        int redeemedUnits = 0 ;
        
        if( numUnitsLeft > 0 ) {
            if( unitsToBeRedeemed <= numUnitsLeft ) {
                redeemedUnits = unitsToBeRedeemed ;
            }
            else {
                redeemedUnits = numUnitsLeft ;
            }
            
            numUnitsLeft -= redeemedUnits ;
            unRedeemedUnits = unitsToBeRedeemed - redeemedUnits ;
        }
        
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
    
    private static final Logger log = Logger.getLogger( EquityHoldingVOBuilder.class ) ;
    
    private EquityHolding holding = null ;

    public EquityHoldingVO buildVO( EquityHolding holding, List<EquityTxn> txns ) {
        
        this.holding = holding ;
        
        EquityHoldingVO vo = new EquityHoldingVO( holding ) ;
        List<EquityLot> lots = processTxns( txns ) ;
        
        vo.setLtcgQty( computeLTCGQty( lots ) ) ;
        if( vo.getQuantity() > 0 ) {
            computeTax( vo ) ;
        }
        return vo ;
    }
    
    // NOTE: The transactions are assumed to be in ascending order. Implying
    // that the first transaction is assumed to be a buy transaction.
    private List<EquityLot> processTxns( List<EquityTxn> txns ) {
        
        List<EquityLot> lots = new ArrayList<>() ;
        
        if( txns != null && !txns.isEmpty() ) {
            
            int index = -1 ;
            
            for( EquityTxn txn : txns ) {
                index++ ;
                String action = txn.getAction() ;
                
                if( action.equalsIgnoreCase( "buy" ) ) {
                    
                    lots.add( new EquityLot( txn.getQuantity(), 
                                             txn.getTxnDate() ) ) ;
                }
                else if( action.equalsIgnoreCase( "sell" ) ) {
                    
                    if( index == 0 ) {
                        // This situation should not arise. This check is for
                        // a sanity check.
                        log.error( "Equity " + holding.getSymbolIcici() + 
                                   " (" + holding.getOwnerName() + ")" +
                                   " hId=" + holding.getId() + " " +
                                   " has first transaction as a sell transaction." ) ;
                    }
                    
                    // A sell transaction quantity can span across multiple 
                    // purchase lots. We keep eating away at all the lots
                    // till the time we have quenched the sell quantity. 
                    // It is assumed that the sell quantity will always be equal
                    // to or less than the remaining buy shares.
                    int unRedeemedUnits = txn.getQuantity() ;
                    
                    Iterator<EquityLot> allLotsIter = lots.iterator() ;
                    while( unRedeemedUnits > 0 && allLotsIter.hasNext() ) {
                        
                        EquityLot lot = allLotsIter.next() ;
                        if( lot.numUnitsLeft > 0 ) {
                            unRedeemedUnits = lot.redeemUnits( unRedeemedUnits ) ;
                        }
                    }
                    
                    if( unRedeemedUnits > 0 ) {
                        
                        // This situation should not arise. This check is for
                        // a sanity check.
                        log.error( "Equity " + holding.getSymbolIcici() + 
                                   "(" + holding.getOwnerName() + ") " +
                                   " hId=" + holding.getId() + " " +
                                   " has more sell quantity than buy quantity." ) ;
                    }
                }
                else {
                    throw new RuntimeException( "Unknown equity txn action - " + action ) ;
                }
            }
        }
        return lots ;
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
        
        float brokerage = 0 ;
        float totalTax = 0 ;
        float valuePostTax = 0 ;
        float profitPostTax = 0 ;
        float profitPctPostTax = 0 ;
        
        if( vo.getOwnerName().equals( "Sandeep" ) ) {
            brokerage = (float)(vo.getValueAtMktPrice() * (0.24/100)) ;
        }
        else {
            brokerage = (float)(vo.getValueAtMktPrice() * (0.77/100)) ;
        }
        
        ltcgCost = ltcgQty * vo.getAvgCostPrice() ;
        ltcgValue = ltcgQty * vo.getCurrentMktPrice() ;
        ltcgProfit = ltcgValue - ltcgCost ;
        ltcgTax = ltcgProfit > 0 ? 0.1f * ltcgProfit : 0 ;
        
        stcgCost = stcgQty * vo.getAvgCostPrice() ;
        stcgValue = stcgQty * vo.getCurrentMktPrice() ;
        stcgProfit = stcgValue - stcgCost ;
        stcgTax = stcgProfit > 0 ? 0.3f * stcgProfit : 0 ;
        
        totalTax = ltcgTax + stcgTax ;
        valuePostTax = vo.getValueAtMktPrice() - totalTax - brokerage ;
        profitPostTax = valuePostTax - vo.getValueAtCost() ;
        profitPctPostTax = ( profitPostTax / vo.getValueAtCost() ) * 100 ; 
        
        vo.setValuePostTax( valuePostTax ) ;
        vo.setProfitPostTax( profitPostTax ) ;
        vo.setProfitPctPostTax( profitPctPostTax ) ;
        vo.setTaxAmount( totalTax ) ;
        vo.setBrokerageAmount( brokerage ) ;
    }
}
