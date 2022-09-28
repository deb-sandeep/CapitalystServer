package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.CascadeType ;
import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.ManyToOne ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_daily_gain" )
public class EquityDailyGain {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    @ManyToOne( cascade= {CascadeType.MERGE} )
    @JoinColumn( name="holding_id" )
    private EquityHolding holding = null ;

    private Date  date       = null ;
    private int   quantity   = 0    ;

    @Column( precision=16, scale=2 )
    private float investmentValue = 0.0f ;

    @Column( precision=16, scale=2 )
    private float closingUnitPrice = 0.0f ;

    @Column( precision=16, scale=2 )
    private float marketValue = 0.0f ;

    @Column( precision=16, scale=2 )
    private float dayChange = 0.0f ;

    @Column( precision=16, scale=2 )
    private float dayChangePct = 0.0f ;
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "EquityHolding [\n" ) ; 
        
        builder.append( "  holdingId        = " + holding.getId()  + "\n" ) ;
        builder.append( "  date             = " + date             + "\n" ) ;
        builder.append( "  investmentValue  = " + investmentValue  + "\n" ) ;
        builder.append( "  quantity         = " + quantity         + "\n" ) ;
        builder.append( "  closingUnitPrice = " + closingUnitPrice + "\n" ) ;
        builder.append( "  marketValue      = " + marketValue      + "\n" ) ;
        builder.append( "  dayChange        = " + dayChange        + "\n" ) ;
        builder.append( "  dayChangePct     = " + dayChangePct     + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}