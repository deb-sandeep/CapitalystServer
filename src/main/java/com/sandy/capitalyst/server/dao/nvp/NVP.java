package com.sandy.capitalyst.server.dao.nvp;

import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "nvp" )
public class NVP {
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MM-yyyy hh:mm:ss" ) ;

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String name = null ;
    private String value = null ;
    
    public NVP() {}
    
    public NVP( String name, String value ) {
        this.name = name ;
        this.value = value ;
    }
    
    public Integer getIntValue() {
        return Integer.valueOf( value ) ;
    }
    
    public Boolean getBooleanValue() {
        return Boolean.valueOf( value ) ;
    }
    
    public void setValue( Integer i ) {
        this.value = i.toString() ;
    }
    
    public void setValue( Boolean b ) {
        this.value = b.toString() ;
    }
    
    public Date getDateValue() {
        try {
            return SDF.parse( value ) ;
        }
        catch( ParseException e ) {
            e.printStackTrace() ;
        }
        return null ;
    }
    
    public void setValue( Date date ) {
        this.value = SDF.format( date ) ;
    }
    
    public void setValue( String[] values ) {
        this.value = String.join( ",", values ) ;
    }
    
    public String[] getArrayValue() {
        return this.value.split( "," ) ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "NVP [\n" ) ; 
        builder.append( "   name  = " + this.name  + "\n" ) ;
        builder.append( "   value = " + this.value + "\n" ) ;
        builder.append( "]" ) ;
        return builder.toString() ;
    }
}

