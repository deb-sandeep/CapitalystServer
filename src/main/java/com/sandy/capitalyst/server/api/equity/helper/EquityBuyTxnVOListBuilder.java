package com.sandy.capitalyst.server.api.equity.helper;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.api.equity.vo.EquityBuyTxnVO ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

public class EquityBuyTxnVOListBuilder {
    
    
    public List<EquityBuyTxnVO> buildBuyTxnVOList( EquityHolding holding,
                                                   List<EquityTxn> txnList ) {
        
        List<EquityBuyTxnVO> buyTxnVOList = new ArrayList<>() ;
        
        for( EquityTxn buyTxn : txnList ) {
            
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
        
        return buyTxnVOList ;
    }
}
