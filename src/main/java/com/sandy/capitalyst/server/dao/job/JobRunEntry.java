package com.sandy.capitalyst.server.dao.job;

import java.util.Date ;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.JoinColumn ;
import jakarta.persistence.ManyToOne ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;
import lombok.Data;

@Data
@Entity
@Table( name = "job_run" )
public class JobRunEntry implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "jrPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "job_run_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="jrPkGen" )
    private Integer id = null ;
    
    @ManyToOne
    @JoinColumn( name="job_id" )
    @JsonIgnore
    private JobEntry job = null ;
    
    private Date date = null ;
    private int duration = 0 ;
    private String result = null ;
    private String remarks = null ;

    public JobRunEntry() {}

    @JsonProperty
    public String getJobName() {
        return job.getIdentity() ;
    }

    public String toString() {
        return  "JobRunEntry [\n" +
                "   id = " + this.id + "\n" +
                "   job = " + this.job + "\n" +
                "   date = " + this.date + "\n" +
                "   duration = " + this.duration + "\n" +
                "   result = " + this.result + "\n" +
                "   remarks = " + this.remarks + "\n" +
                "]";
    }
}
