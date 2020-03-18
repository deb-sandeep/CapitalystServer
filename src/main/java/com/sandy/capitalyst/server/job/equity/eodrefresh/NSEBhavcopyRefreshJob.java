package com.sandy.capitalyst.server.job.equity.eodrefresh;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;

@DisallowConcurrentExecution
public class NSEBhavcopyRefreshJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( NSEBhavcopyRefreshJob.class ) ;
    
    @Override
    protected void preExecute( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "NSEBhavcopyRefreshJob preExecute" ) ;
    }

    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "NSEBhavcopyRefreshJob executeJob" ) ;
    }
}