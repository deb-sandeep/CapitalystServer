package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_eq_data" )
public class HistoricEQData {

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
}