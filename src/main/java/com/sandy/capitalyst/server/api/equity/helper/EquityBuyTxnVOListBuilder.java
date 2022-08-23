package com.sandy.capitalyst.server.api.equity.helper;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.api.equity.vo.EquityBuyTxnVO ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

public class EquityBuyTxnVOListBuilder {
    
    private List<EquityBuyTxnVO> buyTxnVOList = new ArrayList<>() ;
    
    private IndividualEquityHoldingVO holding   = null ;

    public List<EquityBuyTxnVO> buildBuyTxnVOList( EquityHolding eh,
                                                   List<EquityTxn> txnList ) {
        
        holding = new EquityHoldingVOBuilder().buildVO( eh, txnList ) ;
        
        aggregateBuyDayTxns( txnList ) ;
        
        return buyTxnVOList ;
    }

    private void aggregateBuyDayTxns( List<EquityTxn> buyTxns ) {
        
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
}
