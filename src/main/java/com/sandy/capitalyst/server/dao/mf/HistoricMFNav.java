package com.sandy.capitalyst.server.dao.mf;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "historic_mf_nav" )
public class HistoricMFNav {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String isin = null ;
    private Date   date = null ;
    
    @Column( precision=16, scale=2 )
    private float  nav = 0.0F ;
    
    public HistoricMFNav() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
    public Integer getId() {
        return this.id ;
    }

    public String getIsin() {
        return isin ;
    }
    public void setIsin( String isin ) {
        this.isin = isin ;
    }

    public float getNav() {
        return nav ;
    }
    public void setNav( float nav ) {
        this.nav = nav ;
    }

    public Date getDate() {
        return date ;
    }
    public void setDate( Date date ) {
        this.date = date ;
    }
}