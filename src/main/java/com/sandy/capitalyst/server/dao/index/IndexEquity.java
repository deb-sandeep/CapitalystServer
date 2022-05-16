package com.sandy.capitalyst.server.dao.index ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.ManyToOne ;
import javax.persistence.Table ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;

import lombok.Data ;

@Data
@Entity
@Table( name = "index_equities" )
public class IndexEquity {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    @ManyToOne
    @JoinColumn( name="idx_id" )
    private IndexMaster idxMaster = null ;
    
    @ManyToOne
    @JoinColumn( name="equity_id" )
    private EquityMaster eqMaster = null ;
    
    private Float weight = 0F ;
}