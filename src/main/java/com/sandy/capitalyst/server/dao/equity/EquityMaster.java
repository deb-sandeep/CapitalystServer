package com.sandy.capitalyst.server.dao.equity;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;
import javax.persistence.TableGenerator ;

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
    
    @Column( precision=16, scale=2 )
    private Float close = 0F ;

    @Column( precision=16, scale=2 )
    private Float prevClose = 0F ;

    @Column( name = "high_52w", precision=16, scale=2 )
    private Float high52w = 0F ;

    @Column( name = "low_52w", precision=16, scale=2 )
    private Float low52w = 0F ;
    
    @Column( name = "is_etf" )
    private boolean etf = false ;
    
    private String industry = null ;
    private String sector = null ;
    private String detailUrl = null ;
    
    private String description = null ;
}