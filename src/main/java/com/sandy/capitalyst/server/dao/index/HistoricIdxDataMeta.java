package com.sandy.capitalyst.server.dao.index;

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
import javax.persistence.TableGenerator ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_idx_data_meta" )
public class HistoricIdxDataMeta {

    @Id
    @TableGenerator(
        name            = "histIdxDataMetaPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "hist_idx_data_meta_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="histIdxDataMetaPkGen" )
    private Integer id = null ;
    
    @ManyToOne( cascade= {CascadeType.MERGE} )
    @JoinColumn( name="idx_id" )
    private IndexMaster index = null ;
    
    @Column( name = "idx_name" )
    private String indexName = null ;
    
    private Integer numRecords = 0 ;
    private Date earliestEodDate = null ;
    private Date lastUpdate = null ;
}