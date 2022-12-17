package com.sandy.capitalyst.server.dao.idgen ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "id_gen" )
public class IDGen {
    
    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String genKey ;
    private int    genValue ;
    
    public IDGen() {}
    
    public String toString() {
        StringBuilder builder = new StringBuilder( "IDGen [\n" ) ; 
        builder.append( "   genKey    = " + this.genKey    + "\n" ) ;
        builder.append( "   genValue  = " + this.genValue  + "\n" ) ;
        builder.append( "]" ) ;
        return builder.toString() ;
    }
}

