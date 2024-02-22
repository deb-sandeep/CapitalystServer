package com.sandy.capitalyst.server.api.job;

import com.sandy.capitalyst.server.dao.fixed_deposit.FixedDeposit;
import com.sandy.capitalyst.server.dao.job.JobRunEntry;
import com.sandy.capitalyst.server.dao.job.repo.JobEntryRepo;
import com.sandy.capitalyst.server.dao.job.repo.JobRunEntryRepo;
import com.sandy.capitalyst.server.dao.job.repo.JobRunEntrySpecifications;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.*;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;

import java.util.Date;
import java.util.List;

import static com.sandy.capitalyst.server.dao.job.repo.JobRunEntrySpecifications.* ;

@RestController
public class JobController {

    private static final Logger log = Logger.getLogger( JobController.class ) ;

    private JobRunEntryRepo jreRepo = null ;
    private JobEntryRepo jeRepo = null ;

    @Autowired
    public void setJobRunEntryRepo( JobRunEntryRepo repo ) {
        this.jreRepo = repo ;
    }

    @Autowired
    public void setJobEntryRepo( JobEntryRepo repo ) {
        this.jeRepo = repo ;
    }
    
    @PostMapping( "/Job/TriggerNow/{jobName}" ) 
    public ResponseEntity<APIMsgResponse> triggerJob(
                                     @PathVariable String jobName ) {
        try {
            log.debug( "Triggering job = " + jobName ) ;
            CapitalystServer.getApp()
                            .getScheduler()
                            .triggerJob( jobName ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIMsgResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Triggering Job " + jobName, e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @GetMapping( "/Job/RunStatusEntries" )
    public ResponseEntity<List<JobRunEntry>> getJobRunStatusEntries(
            @RequestParam( name="offset"   , defaultValue="0"  , required = false ) Integer offset,
            @RequestParam( name="batchSize", defaultValue="100", required = false ) Integer batchSize ) {
        try {
            List<JobRunEntry> entries = jreRepo.getBatchOfRecords( offset, batchSize ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting job run status entries.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @GetMapping( "/Job/SearchRunStatusEntries" )
    public ResponseEntity<Page<JobRunEntry>> searchJobRunStatusEntries(
            @RequestParam( name="jobName",  required = false ) String[] jobNames,
            @RequestParam( name="result",   required = false ) String result,
            @RequestParam( name="fromDate", required = false ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
            @RequestParam( name="toDate",   required = false ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
            @RequestParam( name="pageNum",  required = false, defaultValue = "0" ) int pageNum,
            @RequestParam( name="pageSize", required = false, defaultValue = "50" ) int pageSize ) {

        Specification<JobRunEntry> rootSpec ;

        try {
            Pageable pageable = PageRequest.of( pageNum, pageSize, Sort.Direction.DESC, "id" ) ;
            rootSpec = buildRootSpecification( jobNames, result, fromDate, toDate ) ;
            Page<JobRunEntry> entries = jreRepo.findAll( rootSpec, pageable ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( entries ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting job run status entries.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( null ) ;
        }
    }

    @GetMapping( "/Job/JobNames" )
    public ResponseEntity<String[]> getDistinctJobNames() {

        try {
            String[] jobNames = jeRepo.findDistinctJobNames() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( jobNames ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting distinct job names.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
