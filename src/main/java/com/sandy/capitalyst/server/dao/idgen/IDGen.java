package com.sandy.capitalyst.server.dao.idgen ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "id_gen" )
public class IDGen {
    
    @Id
    @GeneratedValue( strategy=GenerationType.IDENTITY )
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
