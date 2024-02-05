package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_eq_data_meta" )
public class HistoricEQDataMeta {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String symbolNse = null ;
    private Integer numRecords = 0 ;
    private Date earliestEodDate = null ;
    private Date lastUpdate = null ;
}
