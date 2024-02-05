package com.sandy.capitalyst.server.dao.index ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.JoinColumn ;
import jakarta.persistence.ManyToOne ;
import jakarta.persistence.Table ;

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
    
    private String idxName = null ;
    private String equitySymbol = null ;
}
