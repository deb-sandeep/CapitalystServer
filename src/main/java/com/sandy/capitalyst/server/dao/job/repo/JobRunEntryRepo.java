package com.sandy.capitalyst.server.dao.job.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.job.JobRunEntry ;

public interface JobRunEntryRepo 
    extends CrudRepository<JobRunEntry, Integer> {
}
