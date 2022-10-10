package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_eq_data_meta" )
public class HistoricEQDataMeta {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String symbolNse = null ;
    private Date earliestEodDate = null ;
    private Date lastUpdate = null ;
}