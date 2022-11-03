package com.sandy.capitalyst.server.test.core.asynctask;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.asynctask.AsyncTask ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskHandle ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskManager ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskPartResult ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus ;

public class TestAsyncTask extends AsyncTask {

    private static final Logger log = Logger.getLogger( TestAsyncTask.class ) ;
    
    protected TestAsyncTask() {
        super( "TestAsyncTask" ) ;
    }

    public int getNumSegments() {
        return 5 ;
    }

    protected void execute() throws Exception {
        for( int i=1; i<=getNumSegments(); i++ ) {
            super.setCurSegment( i ) ;
            super.setCurSegmentSize( 10 ) ;
            for( int j=0; j<10; j++ ) {
                super.setCurSegmentCompletion( j+1 ) ;
                super.addMessage( "Message " + i + " " + j ) ;
                Thread.sleep( 100 ) ;
            }
        }
    }
    
    public static void main( String[] args ) throws Exception {
        
        AsyncTaskManager mgr    = AsyncTaskManager.instance() ;
        TestAsyncTask    task   = new TestAsyncTask() ;
        AsyncTaskHandle  handle = mgr.submitTask( task ) ;
        
        AsyncTaskPartResult result = mgr.getPartResult( handle.getTaskId() ) ;
        
        while( result.getRunState() != AsyncTaskRunStatus.COMPLETED ) {

            log.debug( "Run state = " + result.getRunState() ) ;
            if( result.getRunState() == AsyncTaskRunStatus.EXECUTING ) {
                log.debug( "-> Current segment = " + result.getProgress().getCurrSegNumber() ) ;
                log.debug( "-> Current segment completion = " + result.getProgress().getCurrSegCompletion() ) ;
                result.getMessages().forEach( log::debug ) ;
                log.debug( "" ) ;
            }
            Thread.sleep( 100 ) ;
            result = mgr.getPartResult( handle.getTaskId() ) ;
        }
        
    }
}
