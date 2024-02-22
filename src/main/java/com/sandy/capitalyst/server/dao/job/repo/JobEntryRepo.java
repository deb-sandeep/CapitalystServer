package com.sandy.capitalyst.server.dao.job.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.job.JobEntry ;

public interface JobEntryRepo 
    extends CrudRepository<JobEntry, Integer> {
    
    JobEntry findByIdentity( String identity ) ;

    @Query( "SELECT DISTINCT je.identity FROM JobEntry je" )
    String[] findDistinctJobNames() ;
}
