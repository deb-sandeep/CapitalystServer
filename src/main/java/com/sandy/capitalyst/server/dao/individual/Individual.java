package com.sandy.capitalyst.server.dao.individual;

import java.sql.Date ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

@Entity
@Table( name = "individual" )
public class Individual {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id = null ;
    
    private String firstName = null ;
    private String lastName = null ;
    private Date birthDate = null ;
    private String panNumber = null ;
    private String aadharNumber = null ;

    public Individual() {}

    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setFirstName( String val ) {
        this.firstName = val ;
    }
        
    public String getFirstName() {
        return this.firstName ;
    }

    public void setLastName( String val ) {
        this.lastName = val ;
    }
        
    public String getLastName() {
        return this.lastName ;
    }

    public void setBirthDate( Date val ) {
        this.birthDate = val ;
    }
        
    public Date getBirthDate() {
        return this.birthDate ;
    }

    public void setPanNumber( String val ) {
        this.panNumber = val ;
    }
        
    public String getPanNumber() {
        return this.panNumber ;
    }

    public void setAadharNumber( String val ) {
        this.aadharNumber = val ;
    }
        
    public String getAadharNumber() {
        return this.aadharNumber ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "Individual [\n" ) ; 

        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   firstName = " + this.firstName + "\n" ) ;
        builder.append( "   lastName = " + this.lastName + "\n" ) ;
        builder.append( "   dob = " + this.birthDate + "\n" ) ;
        builder.append( "   panNumber = " + this.panNumber + "\n" ) ;
        builder.append( "   aadharNumber = " + this.aadharNumber + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
