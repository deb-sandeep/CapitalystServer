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
@Table( name = "equity_monitor" )
public class EquityMonitor {

    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private Integer id = null ;
    
    private String isin = null ;
    private String symbolNse = null ;
    private String symbolIcici = null ;
    private Date   dateAdded = null ;
}
