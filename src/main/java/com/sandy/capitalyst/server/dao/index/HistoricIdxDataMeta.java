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

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_idx_data_meta" )
public class HistoricIdxDataMeta {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
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