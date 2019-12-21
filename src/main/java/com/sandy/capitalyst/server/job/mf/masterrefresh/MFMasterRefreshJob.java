package com.sandy.capitalyst.server.job.mf.masterrefresh;

import org.apache.log4j.Logger ;
import org.quartz.Job ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

public class MFMasterRefreshJob implements Job {
    
    private static final Logger log = Logger.getLogger( MFMasterRefreshJob.class ) ;

    @Override
    public void execute( JobExecutionContext context )
            throws JobExecutionException {
        
        log.debug( "Executing Mututal Fund master refresh" ) ;
    }
}
