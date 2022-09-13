package com.sandy.capitalyst.server.dao.breeze;

import java.util.Date ;

import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "breeze_invocation_stats" )
public class BreezeInvocationStats {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id ;
    
    private Date date ;
    private String userName ;
    private String apiId ;
    private int numCalls ;
    private int avgTime ;
    private int minTime ;
    private int maxTime ;
}
