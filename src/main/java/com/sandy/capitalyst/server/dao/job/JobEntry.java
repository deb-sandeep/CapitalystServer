package com.sandy.capitalyst.server.dao.job;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "job" )
public class JobEntry {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    private String identity = null ;
    private int lastRunId = 0 ;
    private String state = null ;

    public JobEntry() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setIdentity( String val ) {
        this.identity = val ;
    }
        
    public String getIdentity() {
        return this.identity ;
    }

    public void setLastRunId( int val ) {
        this.lastRunId = val ;
    }
        
    public int getLastRunId() {
        return this.lastRunId ;
    }

    public void setState( String val ) {
        this.state = val ;
    }
        
    public String getState() {
        return this.state ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "JobEntry [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   identity = " + this.identity + "\n" ) ;
        builder.append( "   lastRunId = " + this.lastRunId + "\n" ) ;
        builder.append( "   state = " + this.state + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}

