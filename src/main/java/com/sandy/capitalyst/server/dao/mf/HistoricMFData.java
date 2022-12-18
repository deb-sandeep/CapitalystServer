package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;
import javax.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_mf_data" )
public class HistoricMFData implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "hmdPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "historic_mf_data_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="hmdPkGen" )
    private Integer id = null ;
    
    private String isin = null ;
    private Date   date = null ;
    
    @Column( precision=16, scale=2 )
    private float nav = 0.0F ;
}