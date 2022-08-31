package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.vo.EquityBuyTxnVO ;
import com.sandy.capitalyst.server.api.equity.vo.EquitySellTxnVO ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityDailyGainRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityDailyGainRepo.SparklineData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

class EquityBuyLot {
    
    private EquityTxn buyTxn = null ;
    private EquityBuyTxnVO buyTxnVO = null ;
    
    public EquityBuyLot( EquityHolding holding, EquityTxn buyTxn ) {
        this.buyTxn   = buyTxn ;
        this.buyTxnVO = new EquityBuyTxnVO( holding, buyTxn ) ;
    }
    
    // Returns the number of units that COULD NOT be redeemed
    public int redeemUnits( int qtyToBeRedeemed, EquityTxn sellTxn ) {
        
        int unRedeemedQty = qtyToBeRedeemed ;
        int redeemedQty = 0 ;
        
        if( buyTxnVO.getQuantityLeft() > 0 ) {
            
            if( qtyToBeRedeemed <= buyTxnVO.getQuantityLeft() ) {
                redeemedQty = qtyToBeRedeemed ;
            }
            else {
                redeemedQty = buyTxnVO.getQuantityLeft() ;
            }
            
            buyTxnVO.redeemQuantity( sellTxn, redeemedQty ) ;
            unRedeemedQty = qtyToBeRedeemed - redeemedQty ;
        }
        
        return unRedeemedQty ;
    }
    
    public int getQuantityLeft() {
        return this.buyTxnVO.getQuantityLeft() ;
    }
    
    public EquityBuyTxnVO getBuyTxnVO() {
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

    public IndividualEquityHoldingVO buildVO( EquityHolding holding, 
                                              List<EquityTxn> txns ) {
        
        EquityMaster              em        = null ;
        EquityMasterRepo          emRepo    = null ;
        EquityIndicatorsRepo      eiRepo    = null ;
        List<EquityBuyLot>        buyLots   = null ;
        IndividualEquityHoldingVO holdingVO = null ;
        
        this.holding = holding ;
        
        emRepo  = getBean( EquityMasterRepo.class ) ;
        eiRepo  = getBean( EquityIndicatorsRepo.class ) ;
        
        holdingVO = new IndividualEquityHoldingVO( holding ) ;
        buyLots = buildBuyLots( holdingVO, txns ) ;

        em = emRepo.findByIsin( holdingVO.getIsin() ) ;
        
        for( EquityBuyLot lot : buyLots ) {
            if( lot.getQuantityLeft() > 0 ) {
                holdingVO.addEquityBuyTxnVO( lot.getBuyTxnVO() ) ;
            }
        }
        
        holdingVO.setLtcgQty( computeLTCGQty( buyLots ) ) ;
        holdingVO.computeTaxOnSell() ;
        holdingVO.setSparklineData( getSparklineData() ) ;
        holdingVO.setDetailUrl( em.getDetailUrl() ) ;
        holdingVO.setIndicators( eiRepo.findByIsin( holding.getIsin() ) ) ;
        
        return holdingVO ;
    }
    
    private List<Integer> getSparklineData() {
        
        EquityDailyGainRepo edgRepo = getBean( EquityDailyGainRepo.class ) ;
        
        List<Integer> list = new ArrayList<>() ;
        List<EquitySellTxnVO> sellTxns = null ;
        List<SparklineData> slData = null ;
        Map<Date, EquitySellTxnVO> sellTxnMap = new HashMap<>() ;
        
        EquitySellTxnVOListBuilder helper = new EquitySellTxnVOListBuilder() ;
        
        Date today = new Date() ;
        Date startDate = DateUtils.addDays( today, -15 ) ;
        
        slData = edgRepo.getSparklineData( holding.getId(), startDate, today ) ;
        sellTxns = helper.builtSellTxnVOList( holding, startDate, today ) ;
        
        for( EquitySellTxnVO sellTxn : sellTxns ) {
            sellTxnMap.put( sellTxn.getTxnDate(), sellTxn ) ;
        }
        
        for( SparklineData sl : slData ) {
            Date    date      = sl.getDate() ;
            Float   dayChange = sl.getDayChange() ;
            
            EquitySellTxnVO sellTxn = sellTxnMap.get( date ) ;
            if( sellTxn != null ) {
                dayChange += sellTxn.getPat() ;
            }
            
            list.add( dayChange.intValue() ) ;
        }
        
        return list ;
    }
    
    // NOTE: The transactions are assumed to be in ascending order. Implying
    // that the first transaction is a buy transaction.
    private List<EquityBuyLot> buildBuyLots( IndividualEquityHoldingVO holdingVO, 
                                             List<EquityTxn> txns ) {
        
        List<EquityBuyLot> buyLots = new ArrayList<>() ;
        
        if( txns == null || txns.isEmpty() ) {
            return buyLots ;
        }
        
        for( int i=0; i<txns.size(); i++ ) {
            
            EquityTxn txn    = txns.get( i ) ;
            String    action = txn.getAction() ;
            
            if( i==0 && action.equalsIgnoreCase( "sell" ) ) {
                // This situation should not arise. This check is for
                // catching any data integrity issues.
                log.error( "Equity " + holding.getSymbolIcici() + 
                           " (" + holding.getOwnerName() + ")" +
                           " hId=" + holding.getId() + " " +
                           " has first transaction as a sell transaction." ) ;
            }
            
            
            if( action.equalsIgnoreCase( "buy" ) ) {
                
                buyLots.add( new EquityBuyLot( holdingVO.getBaseHolding(), txn ) ) ;
            }
            else if( action.equalsIgnoreCase( "sell" ) ) {
                
                // A sell transaction quantity can span across multiple 
                // purchase lots. We keep eating away at all the lots
                // till the time we have quenched the sell quantity. 
                // It is assumed that the sell quantity will always be equal
                // to or less than the remaining buy shares.
                int unRedeemedUnits = txn.getQuantity() ;
                
                Iterator<EquityBuyLot> buyLotsIter = buyLots.iterator() ;
                while( unRedeemedUnits > 0 && buyLotsIter.hasNext() ) {
                    
                    EquityBuyLot lot = buyLotsIter.next() ;
                    if( lot.getQuantityLeft() > 0 ) {
                        unRedeemedUnits = lot.redeemUnits( unRedeemedUnits,
                                                           txn ) ;
                    }
                }
                
                if( unRedeemedUnits > 0 ) {
                    
                    // This situation should not arise. This check is for
                    // highlighting any data inconsistency issues.
                    log.error( "Equity " + holding.getSymbolIcici() + 
                               "(" + holding.getOwnerName() + ") " +
                               " hId=" + holding.getId() + " " +
                               " has more sell quantity than buy quantity." ) ;
                }
            }
            else {
                throw new RuntimeException( "Unknown equity buyTxn action - " 
                                            + action ) ;
            }
        }
        return buyLots ;
    }

    private int computeLTCGQty( List<EquityBuyLot> lots ) {
        
        int ltcgQty = 0 ;
        for( EquityBuyLot lot : lots ) {
            if( lot.getQuantityLeft() > 0 ) {
                if( lot.qualifiesForLTCG() ) {
                    ltcgQty += lot.getQuantityLeft() ;
                }
            }
        }
        return ltcgQty ;
    }
}
