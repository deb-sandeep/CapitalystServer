package com.sandy.capitalyst.server.dao.breeze;

import java.util.Date ;

import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;
import jakarta.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "breeze_invocation_stats" )
public class BreezeInvocationStats implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "bisPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "breeze_invocation_stats_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="bisPkGen" )
    private Integer id ;
    
    private Date date ;
    private String userName ;
    private String apiId ;
    private int numCalls ;
    private int avgTime ;
    private int minTime ;
    private int maxTime ;
}
