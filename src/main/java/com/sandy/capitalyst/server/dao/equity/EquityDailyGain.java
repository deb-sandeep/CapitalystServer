package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import jakarta.persistence.CascadeType ;
import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.JoinColumn ;
import jakarta.persistence.ManyToOne ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_daily_gain" )
public class EquityDailyGain implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "edgPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "equity_daily_gain_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="edgPkGen" )
    private Integer id = null ;
    
    @ManyToOne( cascade= {CascadeType.MERGE} )
    @JoinColumn( name="holding_id" )
    private EquityHolding holding = null ;

    private Date  date       = null ;
    private int   quantity   = 0    ;

    @Column( precision=16 )
    private float investmentValue = 0.0f ;

    @Column( precision=16 )
    private float closingUnitPrice = 0.0f ;

    @Column( precision=16 )
    private float marketValue = 0.0f ;

    @Column( precision=16 )
    private float dayChange = 0.0f ;

    @Column( precision=16 )
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
