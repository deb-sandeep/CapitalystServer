package com.sandy.capitalyst.server.dao.job.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.job.JobRunEntry ;

public interface JobRunEntryRepo extends
        JpaRepository<JobRunEntry, Integer>,
        IDCompressor,
        JpaSpecificationExecutor<JobRunEntry> {

    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   job_run "
          + "ORDER BY " 
          + "   id DESC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<JobRunEntry> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Query( nativeQuery = true,
            value =
            "SELECT "
            + "   COUNT(*) "
            + "FROM "
            + "   job_run "
    )
    int getNumRecords() ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   job_run " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
