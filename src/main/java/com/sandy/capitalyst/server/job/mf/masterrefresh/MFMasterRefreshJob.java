package com.sandy.capitalyst.server.job.mf.masterrefresh;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;

@DisallowConcurrentExecution
public class MFMasterRefreshJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( MFMasterRefreshJob.class ) ;

    private static final String SK_COUNTER = "counter" ;
    
    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
                                       throws Exception {
        log.debug( "Executing MFMasterRefreshJob" ) ;
        Integer counter = state.getStateAsInteger( SK_COUNTER ) ;
        log.debug( "Got counter = " + counter ) ;
        if( counter == null ) {
            counter = 1 ;
        }
        else {
            counter++ ;
        }
        log.debug( "Storing counter = " + counter ) ;
        state.setState( SK_COUNTER, counter ) ;
    }
}
