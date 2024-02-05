package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_txn" )
public class EquityTxn implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "etxnPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "equity_txn_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="etxnPkGen" )
    private Integer id = null ;
    
    private int    holdingId       = 0 ;
    private String orderId         = null ;
    private String tradeId         = null ;
    private String symbolIcici     = null ;
    private String action          = null ;
    private int    quantity        = 0 ;
    private Date   txnDate         = null ;
    private String settlementId    = null ;
    private String exchangeTradeId = null ;

    @Column( precision=16 )
    private float txnPrice = 0.0f ;
    
    @Column( precision=16 )
    private float brokerage = 0.0f ;
    
    @Column( precision=16 )
    private float txnCharges = 0.0f ;
    
    @Column( precision=16 )
    private float stampDuty = 0.0f ;
    
    public EquityTxn() {}
    
    public EquityTxn( EquityTxn blueprint ) {
        
        this.id              = blueprint.id ;
        this.holdingId       = blueprint.holdingId ;
        this.orderId         = blueprint.orderId ;
        this.tradeId         = blueprint.tradeId ;
        this.symbolIcici     = blueprint.symbolIcici ;
        this.action          = blueprint.action ;
        this.quantity        = blueprint.quantity ;
        this.txnDate         = blueprint.txnDate ;
        this.settlementId    = blueprint.settlementId ;
        this.exchangeTradeId = blueprint.exchangeTradeId ;
        this.txnPrice        = blueprint.txnPrice ;
        this.brokerage       = blueprint.brokerage ;
        this.txnCharges      = blueprint.txnCharges ;
        this.stampDuty       = blueprint.stampDuty ;
    }
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "EquityTxn [\n" ) ; 
        
        builder.append( "    id              = " + id              + "\n" ) ;
        builder.append( "    holdingId       = " + holdingId       + "\n" ) ;
        builder.append( "    orderId         = " + orderId         + "\n" ) ;
        builder.append( "    tradeId         = " + tradeId         + "\n" ) ;
        builder.append( "    symbolIcici     = " + symbolIcici     + "\n" ) ;
        builder.append( "    action          = " + action          + "\n" ) ;
        builder.append( "    quantity        = " + quantity        + "\n" ) ;
        builder.append( "    txnDate         = " + txnDate         + "\n" ) ;
        builder.append( "    settlementId    = " + settlementId    + "\n" ) ;
        builder.append( "    exchangeTradeId = " + exchangeTradeId + "\n" ) ;
        builder.append( "    txnPrice        = " + txnPrice        + "\n" ) ;
        builder.append( "    brokerage       = " + brokerage       + "\n" ) ;
        builder.append( "    txnCharges      = " + txnCharges      + "\n" ) ;
        builder.append( "    stampDuty       = " + stampDuty       + "\n" ) ;        
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
