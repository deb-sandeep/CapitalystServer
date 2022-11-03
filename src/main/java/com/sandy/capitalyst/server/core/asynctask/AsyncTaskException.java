package com.sandy.capitalyst.server.core.asynctask;

public class AsyncTaskException extends Exception {

    private static final long serialVersionUID = 1L ;
    
    public AsyncTaskException( String message, Throwable cause ) {
        super( message, cause ) ;
    }
    
    public AsyncTaskException( String message ) {
        super( message ) ;
    }
}
