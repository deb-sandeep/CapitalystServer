package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "historic_mf_data" )
public class HistoricMFData {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin = null ;
    private Date   date = null ;
    
    @Column( precision=16, scale=2 )
    private float  nav = 0.0F ;
}