package com.sandy.capitalyst.server.job.mf.masterrefresh;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;

@DisallowConcurrentExecution
public class MFMasterRefreshJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( MFMasterRefreshJob.class ) ;
    
    @Override
    protected void preExecute( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "Executing MFMasterRefreshJob" ) ;
    }

    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "Executing MFMasterRefreshJob" ) ;
    }
}
