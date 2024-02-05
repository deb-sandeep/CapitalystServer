package com.sandy.capitalyst.server.dao.index ;

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
@Table( name = "historic_idx_data" )
public class HistoricIdxData implements EntityWithNumericID {
    
    @Id
    @TableGenerator(
        name            = "hidPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "hist_idx_data_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="hidPkGen" )
    private Integer id = null ;
    
    @ManyToOne( cascade= {CascadeType.MERGE} )
    @JoinColumn( name="idx_id" )
    private IndexMaster index = null ;
    
    @Column( name = "idx_name" )
    private String indexName = null ;
    
    private Date date = null ;

    private double open   = 0 ;
    private double high   = 0 ;
    private double low    = 0 ;
    private double close  = 0 ;
    private long   volume = 0 ;
}
