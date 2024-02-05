package com.sandy.capitalyst.server.dao.equity;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_master" )
public class EquityMaster {

    @Id
    @TableGenerator(
        name            = "emPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "equity_master_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="emPkGen" )
    private Integer id = null ;
    
    private String isin = null ;
    private String symbol = null ;
    private String symbolIcici = null ;
    private String name = null ;
    private String mcName = null ;
    
    @Column( precision=16 )
    private Float close = 0F ;

    @Column( precision=16 )
    private Float prevClose = 0F ;

    @Column( name = "high_52w", precision=16 )
    private Float high52w = 0F ;

    @Column( name = "low_52w", precision=16 )
    private Float low52w = 0F ;
    
    @Column( name = "is_etf" )
    private boolean etf = false ;
    
    private String industry = null ;
    private String sector = null ;
    private String detailUrl = null ;
    
    private String description = null ;
}
