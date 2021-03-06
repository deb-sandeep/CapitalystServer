package com.sandy.capitalyst.server.dao.job;

import java.util.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.ManyToOne ;
import javax.persistence.Table ;

@Entity
@Table( name = "job_run" )
public class JobRunEntry {

    @Id
    @GeneratedValue( strategy=GenerationType.AUTO )
    private Integer id = null ;
    
    @ManyToOne
    @JoinColumn( name="job_id" )
    private JobEntry job = null ;
    
    private Date date = null ;
    private int duration = 0 ;
    private String result = null ;
    private String remarks = null ;

    public JobRunEntry() {}
    
    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setJob( JobEntry val ) {
        this.job = val ;
    }
        
    public JobEntry getJob() {
        return this.job ;
    }

    public void setDate( Date val ) {
        this.date = val ;
    }
        
    public Date getDate() {
        return this.date ;
    }

    public void setDuration( int val ) {
        this.duration = val ;
    }
        
    public int getDuration() {
        return this.duration ;
    }

    public void setResult( String val ) {
        this.result = val ;
    }
        
    public String getResult() {
        return this.result ;
    }

    public void setRemarks( String val ) {
        this.remarks = val ;
    }
        
    public String getRemarks() {
        return this.remarks ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "JobRunEntry [\n" ) ; 
        
        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   job = " + this.job + "\n" ) ;
        builder.append( "   date = " + this.date + "\n" ) ;
        builder.append( "   duration = " + this.duration + "\n" ) ;
        builder.append( "   result = " + this.result + "\n" ) ;
        builder.append( "   remarks = " + this.remarks + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
