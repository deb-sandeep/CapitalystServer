package com.sandy.capitalyst.server.core.asynctask;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.asynctask.internal.AsyncTaskWrapper ;
import com.sandy.capitalyst.server.core.log.IndentUtil ;

import static com.sandy.capitalyst.server.core.asynctask.AsyncTaskRunStatus.* ;

public abstract class AsyncTask implements Runnable {

    private static final Logger log = Logger.getLogger( AsyncTask.class ) ;
    
    private AsyncTaskWrapper wrapper = null ;
    private String name = null ;
    
    protected AsyncTask( String name ) {
        this.name = name ;
    }
    
    public void setTaskWrapper( AsyncTaskWrapper wrapper ) {
        this.wrapper = wrapper ;
    }
    
    public String getName() {
        return this.name ;
    }

    public final void run() {
        try {
            IndentUtil.i_clear() ;
            wrapper.setRunStatus( EXECUTING );
            
            log.debug( "Pre Executing AsyncTask " + getName() ) ;
            preExecute() ;
            
            log.debug( "Executing AsyncTask " + getName() ) ;
            execute() ;
        }
        catch( Exception e ) {
            log.error( "Async Task " + getName() + " exception.", e ) ;
            wrapper.addException( e ) ;
        }
        finally {
            try {
                log.debug( "Post Executing AsyncTask " + getName() ) ;
                postExecute() ;
            }
            catch( Exception e ) {
                log.debug( "Exception in post execute for task " + getName(), e ) ;
                wrapper.addException( e ) ;
            }
            wrapper.setRunStatus( COMPLETED ) ;
            IndentUtil.i_clear() ;
        }
    }
    
    protected void addMessage( String message ) {
        wrapper.addMessage( message ) ;
    }
    
    protected void addResult( String key, Object value ) {
        wrapper.addResult( key, value ) ;
    }
    
    protected void setCurSegment( int segmentNumber ) {
        wrapper.setCurSegment( segmentNumber ) ;
    }
    
    protected void setCurSegmentSize( int segmentSize ) {
        wrapper.setCurSegmentSize( segmentSize ) ;
    }
    
    protected void setCurSegmentCompletion( int completedSize ) {
        wrapper.setCurSegmentCompletion( completedSize ) ;
    }
    
    public abstract int getNumSegments() ;
    
    protected void preExecute() throws Exception {} 
    protected abstract void execute() throws Exception ;
    protected void postExecute() throws Exception {}
}
