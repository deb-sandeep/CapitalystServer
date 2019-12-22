package com.sandy.capitalyst.server.dao.job;

import org.springframework.data.repository.CrudRepository ;

public interface JobEntryRepo 
    extends CrudRepository<JobEntry, Integer> {
    
    JobEntry findByIdentity( String identity ) ;
}
