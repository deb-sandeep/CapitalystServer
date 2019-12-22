package com.sandy.capitalyst.server.dao.job;

import org.springframework.data.repository.CrudRepository ;

public interface JobRunEntryRepo 
    extends CrudRepository<JobRunEntry, Integer> {
}
