package com.sandy.capitalyst.server.dao.breeze.repo;

import java.util.Date ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.breeze.BreezeInvocationStats ;

public interface BreezeInvocationStatsRepo 
    extends CrudRepository<BreezeInvocationStats, Integer> {
    
    BreezeInvocationStats findByDateAndUserNameAndApiId( Date date, 
                                                         String userName, 
                                                         String apiId ) ;
}
