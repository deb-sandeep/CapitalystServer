package com.sandy.capitalyst.server.dao.mf;

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
    
    @Column( precision=16 )
    private float nav = 0.0F ;
}
