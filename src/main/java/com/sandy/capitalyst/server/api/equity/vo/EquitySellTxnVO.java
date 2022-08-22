package com.sandy.capitalyst.server.api.equity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false )
public class EquitySellTxnVO extends EquityTxn {
    
    @JsonIgnore
    private IndividualEquityHoldingVO holding = null ;
    
    public EquitySellTxnVO( EquitySellTxnVO vo ) {
        super( vo ) ;
        this.holding = vo.holding ;
    }

    public EquitySellTxnVO( IndividualEquityHoldingVO holding, 
                            EquityTxn sellTxn ) {
        
        super( sellTxn ) ;
        this.holding = holding ;
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

    public void associateBuyTxn( EquityBuyTxnVO buyTxn, int redeemQty ) {
    }
}
