package com.sandy.capitalyst.server.api.equity.helper;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.vo.EquityHoldingVO ;
import com.sandy.capitalyst.server.api.equity.vo.EquityTxnVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

class EquityLot {
    
    private EquityTxn buyTxn = null ;
    private EquityTxnVO buyTxnVO = null ;
    
    public EquityLot( EquityHoldingVO holding, EquityTxn buyTxn ) {
        this.buyTxn       = buyTxn ;
        this.buyTxnVO     = new EquityTxnVO( holding, buyTxn ) ;
    }
    
    // Returns the number of units that COULD NOT be redeemed
    public int redeemUnits( int qtyToBeRedeemed ) {
        
        int unRedeemedQty = qtyToBeRedeemed ;
        int redeemedQty = 0 ;
        
        if( buyTxnVO.getQuantityLeft() > 0 ) {
            
            if( qtyToBeRedeemed <= buyTxnVO.getQuantityLeft() ) {
                redeemedQty = qtyToBeRedeemed ;
            }
            else {
                redeemedQty = buyTxnVO.getQuantityLeft() ;
            }
            
            buyTxnVO.redeemQuantity( redeemedQty ) ;
            unRedeemedQty = qtyToBeRedeemed - redeemedQty ;
        }
        
        return unRedeemedQty ;
    }
    
    public int getQuantityLeft() {
        return this.buyTxnVO.getQuantityLeft() ;
    }
    
    public EquityTxnVO getBuyTxn() {
        return this.buyTxnVO ;
    }
    
    public boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return buyTxn.getTxnDate().before( oneYearPastDate ) ;
    }
}

public class EquityHoldingVOBuilder {
    
    private static final Logger log = Logger.getLogger( EquityHoldingVOBuilder.class ) ;
    
    private EquityHolding holding = null ;

    public EquityHoldingVO buildVO( EquityHolding holding, List<EquityTxn> txns ) {
        
        this.holding = holding ;
        
        EquityHoldingVO holdingVO = new EquityHoldingVO( holding ) ;
        List<EquityLot> lots = processTxns( holdingVO, txns ) ;
        
        for( EquityLot lot : lots ) {
            if( lot.getQuantityLeft() > 0 ) {
                holdingVO.addEquityTxnVO( lot.getBuyTxn() ) ;
            }
        }
        
        holdingVO.setLtcgQty( computeLTCGQty( lots ) ) ;
        holdingVO.computeTax() ;
        
        return holdingVO ;
    }
    
    // NOTE: The transactions are assumed to be in ascending order. Implying
    // that the first transaction is assumed to be a buy transaction.
    private List<EquityLot> processTxns( EquityHoldingVO holdingVO, 
                                         List<EquityTxn> txns ) {
        
        List<EquityLot> lots = new ArrayList<>() ;
        
        if( txns != null && !txns.isEmpty() ) {
            
            int index = -1 ;
            
            for( EquityTxn txn : txns ) {
                index++ ;
                String action = txn.getAction() ;
                
                if( action.equalsIgnoreCase( "buy" ) ) {
                    
                    lots.add( new EquityLot( holdingVO, txn ) ) ;
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
                        if( lot.getQuantityLeft() > 0 ) {
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
                    throw new RuntimeException( "Unknown equity buyTxn action - " + action ) ;
                }
            }
        }
        return lots ;
    }

    private int computeLTCGQty( List<EquityLot> lots ) {
        
        int ltcgQty = 0 ;
        for( EquityLot lot : lots ) {
            if( lot.getQuantityLeft() > 0 ) {
                if( lot.qualifiesForLTCG() ) {
                    ltcgQty += lot.getQuantityLeft() ;
                }
            }
        }
        return ltcgQty ;
    }
    
}
