package com.sandy.capitalyst.server.core.asynctask.internal;

import static com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus.COMPLETED ;
import static com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus.EXECUTING ;
import static com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus.YET_TO_EXECUTE ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.capitalyst.server.core.asynctask.AsyncTask ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskHandle ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskMessage ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskPartResult ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskProgress ;
import com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus ;

public class AsyncTaskWrapper {

    private AsyncTask              task      = null ;
    private AsyncTaskHandle        handle    = null ;
    private AsyncTaskProgress      progress  = new AsyncTaskProgress() ;
    private AsyncTaskRunStatus     runStatus = YET_TO_EXECUTE ;
    private List<AsyncTaskMessage> messages  = new ArrayList<>() ;
    private Map<String, Object>    results   = new HashMap<>() ;
    
    private long taskCreationTime ;
    private long executionStartTime ;
    private long executionEndTime ;
    
    public AsyncTaskWrapper( AsyncTask task, AsyncTaskHandle handle ) {
        this.task = task ;
        this.handle = handle ;
        this.taskCreationTime = System.currentTimeMillis() ;
        
        progress.setNumSegments( this.task.getNumSegments() ) ;
    }
    
    public AsyncTaskHandle getHandle() {
        return this.handle ;
    }
    
    public synchronized void setRunStatus( AsyncTaskRunStatus runStatus ) {
        this.runStatus = runStatus ;
        if( runStatus == EXECUTING ) {
            this.executionStartTime = System.currentTimeMillis() ;
        }
        else if( runStatus == COMPLETED ) {
            this.executionEndTime = System.currentTimeMillis() ;
        }
    }
    
    @SuppressWarnings( "incomplete-switch" )
    public synchronized long getElapsedTimeSinceExecution() {
        switch( runStatus ) {
            case YET_TO_EXECUTE:
                return 0 ;
            case EXECUTING:
                return System.currentTimeMillis() - this.executionStartTime ;
            case COMPLETED:
                return this.executionEndTime - this.executionStartTime ;
        }
        return 0 ;
    }
    
    public synchronized long getElapsedTimeSinceCreation() {
        return System.currentTimeMillis() - this.taskCreationTime ;
    }
    
    public synchronized void addMessage( String message ) {
        this.messages.add( new AsyncTaskMessage( message ) ) ;
    }
    
    public synchronized void addException( Exception e ) {
        this.messages.add( new AsyncTaskMessage( e ) ) ;
    }
    
    public synchronized void addException( String msg, Exception e ) {
        this.messages.add( new AsyncTaskMessage( msg, e ) ) ;
    }
    
    public synchronized void addResult( String key, Object val ) {
        this.results.put( key, val ) ;
    }
    
    public void setCurSegment( int segmentNumber ) {
        progress.setCurrSegNumber( segmentNumber ) ;
    }
    
    public void setCurSegmentSize( int segmentSize ) {
        progress.setCurrSegSize( segmentSize ) ;
    }
    
    public void setCurSegmentCompletion( int completedSize ) {
        progress.setCurrSegCompletion( completedSize ) ;
    }
    
    public synchronized void populatePartResult( AsyncTaskPartResult partResult ) {
        
        partResult.setRunState( this.runStatus ) ;
        partResult.setProgress( this.progress ) ;
        partResult.getReturnValues().putAll( this.results ) ;
        partResult.getMessages().addAll( this.messages ) ;
        
        this.results.clear() ;
        this.messages.clear() ;
    }
}
