package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.vo.EquityTxnVO ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityDailyGainRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

class EquityLot {
    
    private EquityTxn buyTxn = null ;
    private EquityTxnVO buyTxnVO = null ;
    
    public EquityLot( IndividualEquityHoldingVO holding, EquityTxn buyTxn ) {
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
    
    public EquityTxnVO getBuyTxnVO() {
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

    public IndividualEquityHoldingVO buildVO( EquityHolding holding, List<EquityTxn> txns ) {
        
        this.holding = holding ;
        
        IndividualEquityHoldingVO holdingVO = new IndividualEquityHoldingVO( holding ) ;
        List<EquityLot> lots = processTxns( holdingVO, txns ) ;
        
        EquityMasterRepo emRepo = getBean( EquityMasterRepo.class ) ;
        EquityMaster em = emRepo.findByIsin( holdingVO.getIsin() ) ;
        
        int totalQuantityLeft = 0 ;
        for( EquityLot lot : lots ) {
            if( lot.getQuantityLeft() > 0 ) {
                holdingVO.addEquityTxnVO( lot.getBuyTxnVO() ) ;
                totalQuantityLeft += lot.getBuyTxnVO().getQuantityLeft() ;
            }
        }
        
        holdingVO.setLtcgQty( computeLTCGQty( lots ) ) ;
        holdingVO.computeTax() ;
        holdingVO.setSparklineData( getSparklineData() ) ;
        holdingVO.setDetailUrl( em.getDetailUrl() ) ;
        
        if( totalQuantityLeft != holding.getQuantity() ) {
            // This implies some transactions are missing which needs to be
            // manually added.
            log.error( "Equity " + holding.getSymbolIcici() + 
                       " (" + holding.getOwnerName() + ")" +
                       " hId=" + holding.getId() + " " +
                       " has missing transactions." ) ;
        }
        return holdingVO ;
    }
    
    private List<Integer> getSparklineData() {
        
        EquityDailyGainRepo edgRepo = getBean( EquityDailyGainRepo.class ) ;
        Date today = new Date() ;
        Date startDate = DateUtils.addDays( today, -15 ) ;
        
        List<Integer> list = new ArrayList<>() ;
        List<Float> dbData = null ;
        
        dbData = edgRepo.getSparklineData( holding.getId(), startDate, today ) ;
        for( Float f : dbData ) {
            list.add( f.intValue() ) ;
        }
        
        return list ;
    }
    
    // NOTE: The transactions are assumed to be in ascending order. Implying
    // that the first transaction is assumed to be a buy transaction.
    private List<EquityLot> processTxns( IndividualEquityHoldingVO holdingVO, 
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
