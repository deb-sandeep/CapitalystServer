package com.sandy.capitalyst.server.core.scheduler;

import java.util.Date ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.quartz.Job ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;
import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.job.JobEntry ;
import com.sandy.capitalyst.server.dao.job.JobEntryRepo ;
import com.sandy.capitalyst.server.dao.job.JobRunEntry ;
import com.sandy.capitalyst.server.dao.job.JobRunEntryRepo ;
import com.sandy.capitalyst.server.util.StringUtil ;

public abstract class CapitalystJob implements Job {

    private static final Logger log = Logger.getLogger( CapitalystJob.class ) ;
    
    public static enum JobResult{ SUCCESS, FAILURE } ;

    private JobEntryRepo jeRepo = null ;
    private JobRunEntryRepo jreRepo = null ;
    private ObjectMapper mapper = new ObjectMapper( new YAMLFactory() ) ;

    @Override
    public final void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        long   startTime = System.currentTimeMillis() ;
        int    duration  = 0 ;
        String remarks   = null ;
        Date   startDate = new Date() ;
        String jobIdentity = null ;
        String serializedJobState = null ;
        
        JobEntry  jobEntry = null ;
        JobResult result   = JobResult.SUCCESS ;
        
        mapper = new ObjectMapper( new YAMLFactory() ) ;
        mapper.findAndRegisterModules() ;

        jobIdentity = context.getJobDetail().getKey().getName() ;
        log.debug( "Executing Job - " + jobIdentity ) ;
        
        getJPARepositories() ;
        jobEntry = getJobEntry( jobIdentity ) ;
        
        try {
            serializedJobState = jobEntry.getState() ;
            JobState jobState = getJobState( serializedJobState ) ;
            preExecute( context, jobState ) ;
            executeJob( context, jobState ) ;
            postExecute( context, jobState ) ;
            serializedJobState = mapper.writeValueAsString( jobState ) ;
        }
        catch( Exception e ) {
            log.error( "Job " + jobIdentity + " failed.", e ) ;
            result = JobResult.FAILURE ;
            remarks = ExceptionUtils.getStackTrace( e ) ;
        }
        finally {
            duration = (int)(( System.currentTimeMillis() - startTime )/1000) ;
        }
        
        saveJobState( duration, remarks, startDate, result, 
                      serializedJobState, jobEntry ) ;
    }

    private void saveJobState( int duration, String remarks, Date startDate,
                               JobResult result, String serializedJobState, 
                               JobEntry jobEntry ) {
        
        JobRunEntry jobRunEntry = null ;
        
        log.debug( "Saving job run entry" ) ;
        jobRunEntry = new JobRunEntry() ;
        jobRunEntry.setJob( jobEntry ) ;
        jobRunEntry.setDate( startDate ) ;
        jobRunEntry.setDuration( duration ) ;
        jobRunEntry.setResult( result.name() ) ;
        jobRunEntry.setRemarks( remarks ) ;
        jobRunEntry = jreRepo.save( jobRunEntry ) ;
        
        log.debug( "Updating job entry" ) ;
        jobEntry.setLastRunId( jobRunEntry.getId() ) ;
        jobEntry.setState( serializedJobState ) ;
        jeRepo.save( jobEntry ) ;
    }
    
    private void getJPARepositories() {
        if( jeRepo == null || jreRepo == null ) {
            jeRepo = CapitalystServer.getBean( JobEntryRepo.class ) ;
            jreRepo = CapitalystServer.getBean( JobRunEntryRepo.class ) ;
        }
    }
    
    private JobEntry getJobEntry( String identity ) {
        
        JobEntry jobEntry = null ;
        jobEntry = jeRepo.findByIdentity( identity ) ;
        if( jobEntry == null ) {
            jobEntry = new JobEntry() ;
            jobEntry.setIdentity( identity ) ;
            jeRepo.save( jobEntry ) ;
        }
        return jobEntry ;
    }
    
    private JobState getJobState( String serializedState ) 
        throws Exception {
        
        JobState jobState = null ;
        if( StringUtil.isEmptyOrNull( serializedState ) ) {
            jobState = new JobState() ;
        }
        else {
            jobState = mapper.readValue( serializedState, JobState.class ) ;
        }
        return jobState ;
    }
    
    protected void preExecute( JobExecutionContext context, JobState state )
        throws Exception {
        // Default no-op
    }
    
    protected abstract void executeJob( JobExecutionContext context,
                                        JobState state ) 
        throws Exception ;

    protected void postExecute( JobExecutionContext context, JobState state )
        throws Exception {
        // Default no-op
    }
}
