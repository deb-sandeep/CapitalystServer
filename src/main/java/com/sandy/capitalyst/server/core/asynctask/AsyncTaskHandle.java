package com.sandy.capitalyst.server.core.asynctask;

import java.util.Date ;

import lombok.Data ;

@Data
public class AsyncTaskHandle {

    private String taskId = null ;
    private int numSegments = 1 ;
    private Date submitTime = null ;
    
    @Override
    public boolean equals( Object obj ) {
        AsyncTaskHandle other = (AsyncTaskHandle) obj ;
        return other.taskId.equals( this.taskId ) ;
    }
    
    @Override
    public int hashCode() {
        return taskId.hashCode() ;
    }
}
