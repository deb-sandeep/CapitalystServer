package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.LinkedList ;
import java.util.List ;
import java.util.Queue ;

import com.sandy.capitalyst.server.api.equity.vo.EquityBuyTxnVO ;
import com.sandy.capitalyst.server.api.equity.vo.EquitySellTxnVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

public class EquitySellTxnVOListBuilder {
    
    private List<EquityTxn>       sellTxns      = new ArrayList<>() ;
    private List<EquityTxn>       buyTxns       = new ArrayList<>() ;
    
    private Queue<EquityBuyTxnVO> buyTxnVOList  = new LinkedList<>() ;
    private List<EquitySellTxnVO> sellTxnVOList = new ArrayList<>() ;
    
    public List<EquitySellTxnVO> builtSellTxnVOList( EquityHolding eh,
                                                     Date startDate, 
                                                     Date endDate ) {
        
        List<EquityTxn> txnList = null ;
        EquityTxnRepo etRepo = getBean( EquityTxnRepo.class ) ;
        List<EquitySellTxnVO> sellTxns = new ArrayList<>() ;
        
        txnList = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( eh.getId() ) ;
        sellTxns.addAll( buildSellTxnVOList( eh, txnList ) ) ;
        
        return sellTxns ;
    }

    public List<EquitySellTxnVO> buildSellTxnVOList( EquityHolding eh,
                                                     List<EquityTxn> txnList ) {
        
        separateBuyAndSellTxns( txnList ) ;
        aggregateBuyDayTxns( eh ) ;
        aggregateSellDayTxns( eh ) ;
        associateBuyTxnsWithSellTxns() ;
        
        return sellTxnVOList ;
    }

    private void separateBuyAndSellTxns( List<EquityTxn> txnList ) {
        
        for( EquityTxn txn : txnList ) {
            if( txn.getAction().equalsIgnoreCase( "buy" ) ) {
                buyTxns.add( txn ) ;
            }
            else {
                sellTxns.add( txn ) ;
            }
        }
    }
    
    private void aggregateBuyDayTxns( EquityHolding holding ) {
        
        for( EquityTxn buyTxn : buyTxns ) {
            
            EquityBuyTxnVO txnVO =  new EquityBuyTxnVO( holding, buyTxn ) ;
            boolean sameDateTxnFound = false ;
            
            for( EquityBuyTxnVO buyTxnVO : buyTxnVOList ) {
                if( buyTxnVO.getTxnDate().equals( buyTxn.getTxnDate() ) ) {
                    buyTxnVO.aggregate( txnVO ) ;
                    sameDateTxnFound = true ;
                    break ;
                }
            }
            
            if( !sameDateTxnFound ) {
                buyTxnVOList.add( txnVO ) ;
            }
        }
    }

    private void aggregateSellDayTxns( EquityHolding holding ) {
        
        for( EquityTxn sellTxn : sellTxns ) {
            
            EquitySellTxnVO txnVO =  new EquitySellTxnVO( holding, sellTxn ) ;
            boolean sameDateTxnFound = false ;
            
            for( EquitySellTxnVO sellTxnVO : sellTxnVOList ) {
                if( sellTxnVO.getTxnDate().equals( sellTxn.getTxnDate() ) ) {
                    sellTxnVO.aggregate( txnVO ) ;
                    sameDateTxnFound = true ;
                    break ;
                }
            }
            
            if( !sameDateTxnFound ) {
                sellTxnVOList.add( txnVO ) ;
            }
        }
    }
    
    private void associateBuyTxnsWithSellTxns() {
        
        for( EquitySellTxnVO sellTxn : sellTxnVOList ) {
            
            int sellQtyLeft = sellTxn.getQuantity() ;
            
            while( sellQtyLeft > 0 ) {
                
                EquityBuyTxnVO buyTxn = buyTxnVOList.peek() ;
                if( buyTxn == null ) {
                    throw new IllegalStateException( "Buy txns quenched." ) ;
                }
                
                int unredeemedBuyQty = buyTxn.getQuantityLeft() ;
                int redeemQty = sellQtyLeft ;
                
                if( sellQtyLeft >= unredeemedBuyQty ) {
                    redeemQty = unredeemedBuyQty ;
                }
                
                buyTxn.redeemQuantity( sellTxn, redeemQty ) ;
                sellTxn.associateBuyTxn( buyTxn.getBaseTxn(), redeemQty ) ;
                
                if( buyTxn.getQuantityLeft() == 0 ) {
                    buyTxnVOList.remove() ;
                }
                
                sellQtyLeft -= redeemQty ;
            }
        }
    }
}
