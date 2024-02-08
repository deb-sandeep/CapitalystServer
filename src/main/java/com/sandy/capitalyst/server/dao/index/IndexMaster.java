package com.sandy.capitalyst.server.dao.index ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "index_master" )
public class IndexMaster {
    
    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
    private Integer id = null ;
    
    private String name = null ;
    private Boolean histEnabled = Boolean.FALSE ;
    private String type = null ;
    private String includedStocksUrl = null ;
    private String description = null ;
}
