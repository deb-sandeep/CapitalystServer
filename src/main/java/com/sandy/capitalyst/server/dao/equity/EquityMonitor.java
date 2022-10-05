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
@Table( name = "equity_monitor" )
public class EquityMonitor {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin = null ;
    private String symbolNse = null ;
    private String symbolIcici = null ;
    private Date   dateAdded = null ;
}