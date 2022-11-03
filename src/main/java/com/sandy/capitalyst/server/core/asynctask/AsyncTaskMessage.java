package com.sandy.capitalyst.server.core.asynctask;

import org.apache.commons.lang.exception.ExceptionUtils ;

import lombok.Data ;

@Data
public class AsyncTaskMessage {

    private long time = 0 ;
    private String message = null ;
    private String exceptionTrace = null ;
    
    public AsyncTaskMessage() {
        this.time = System.currentTimeMillis() ;
    }
    
    public AsyncTaskMessage( String msg ) {
        this() ;
        this.message = msg ;
    }
    
    public AsyncTaskMessage( String msg, Exception e ) {
        this( msg ) ;
        this.exceptionTrace = ExceptionUtils.getFullStackTrace( e ) ;
    }
    
    public AsyncTaskMessage( Exception e ) {
        this( "EXCEPTION: " + ExceptionUtils.getMessage( e ), e ) ;
    }
}
