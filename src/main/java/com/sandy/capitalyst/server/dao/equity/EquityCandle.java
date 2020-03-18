package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "historic_equity_mkt_data" )
public class EquityCandle {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String symbol = null ;
    private long   totalTradeQty = 0 ;
    private long   totalTrades = 0 ;
    private Date   date = null ;
    
    @Column( precision=16, scale=2 )
    private float  open = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float  high = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float  low = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float  close = 0.0F ;
    
    @Column( precision=16, scale=2 )
    private float  totalTradeVal = 0.0F ;
    
    public EquityCandle() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
    public Integer getId() {
        return this.id ;
    }

    public String getSymbol() {
        return symbol ;
    }
    public void setSymbol( String symbol ) {
        this.symbol = symbol ;
    }

    public float getOpen() {
        return open ;
    }
    public void setOpen( float open ) {
        this.open = open ;
    }

    public float getHigh() {
        return high ;
    }
    public void setHigh( float high ) {
        this.high = high ;
    }

    public float getLow() {
        return low ;
    }
    public void setLow( float low ) {
        this.low = low ;
    }

    public float getClose() {
        return close ;
    }
    public void setClose( float close ) {
        this.close = close ;
    }

    public long getTotalTradeQty() {
        return totalTradeQty ;
    }
    public void setTotalTradeQty( long totalTradeQty ) {
        this.totalTradeQty = totalTradeQty ;
    }

    public float getTotalTradeVal() {
        return totalTradeVal ;
    }
    public void setTotalTradeVal( float totalTradeVal ) {
        this.totalTradeVal = totalTradeVal ;
    }

    public long getTotalTrades() {
        return totalTrades ;
    }
    public void setTotalTrades( long totalTrades ) {
        this.totalTrades = totalTrades ;
    }

    public Date getDate() {
        return date ;
    }
    public void setDate( Date date ) {
        this.date = date ;
    }
}