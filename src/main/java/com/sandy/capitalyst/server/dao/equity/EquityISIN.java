package com.sandy.capitalyst.server.dao.equity;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "equity_symbol_isin_map" )
public class EquityISIN {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String symbol = null ;
    private String isin = null ;
    
    public EquityISIN() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
    public Integer getId() {
        return this.id ;
    }

    public String getSymbol() {
        return symbol ;
    }
    public void setSymbol( String symbol ) {
        this.symbol = symbol ;
    }

    public String getIsin() {
        return isin ;
    }

    public void setIsin( String isin ) {
        this.isin = isin ;
    }
}