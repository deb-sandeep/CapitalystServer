package com.sandy.capitalyst.server.core.asynctask;

import java.util.Date ;
import java.util.Map ;
import java.util.concurrent.ConcurrentHashMap ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.asynctask.internal.AsyncTaskWrapper ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

public class AsyncTaskManager {

    private static final Logger log = Logger.getLogger( AsyncTaskManager.class ) ;
    
    private static AsyncTaskManager instance = null ;
    
    public static AsyncTaskManager instance() {
        if( instance == null ) {
            instance = new AsyncTaskManager() ;
        }
        return instance ;
    }
    
    private Map<String, AsyncTaskWrapper> taskMap = new ConcurrentHashMap<>() ;
    private ExecutorService executor = null ;
    
    private AsyncTaskManager() {
        executor = Executors.newFixedThreadPool( 3 ) ;
    }

    public AsyncTaskHandle submitTask( AsyncTask task ) 
        throws AsyncTaskException {
        
        AsyncTaskHandle handle = new AsyncTaskHandle() ;
        handle.setTaskId( generateTaskId( task ) ) ;
        handle.setNumSegments( task.getNumSegments() ) ;
        handle.setSubmitTime( new Date() ) ;
        
        AsyncTaskWrapper wrapper = new AsyncTaskWrapper( task, handle ) ;
        
        try {
            taskMap.put( handle.getTaskId(), wrapper ) ;
            executor.execute( task ) ;
        }
        catch( Exception e ) {
            log.error( "Async task could not be executed.", e ) ;
            throw new AsyncTaskException( "Could not execute async task.", e ) ;
        }
        
        return handle ;
    }
    
    public AsyncTaskPartResult getPartResult( String taskId ) 
        throws AsyncTaskException {
        
        AsyncTaskPartResult partResult = new AsyncTaskPartResult() ;
        AsyncTaskWrapper taskWrapper = taskMap.get( taskId ) ;
        
        if( taskWrapper == null ) {
            partResult.getMessages()
                      .add( new AsyncTaskMessage( "Task does not exist." ) ) ;
        }
        else {
            taskWrapper.populatePartResult( partResult ) ;
        }
        
        return partResult ;
    }
    
    private String generateTaskId( AsyncTask task ) {
        
        StringBuilder sb = new StringBuilder() ;
        sb.append( task.getClass().getName() )
          .append( System.currentTimeMillis() )
          .append( Math.random() ) ;
        
        return StringUtil.getHash( sb.toString() ) ;
    }
}
