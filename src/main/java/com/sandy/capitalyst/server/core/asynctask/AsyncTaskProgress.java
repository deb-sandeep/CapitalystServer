package com.sandy.capitalyst.server.core.asynctask;

import lombok.Data ;

@Data
public class AsyncTaskProgress {

    private int numSegments       = 1 ;
    private int currSegNumber     = 1 ;
    private int currSegSize       = 1 ;
    private int currSegCompletion = 0 ;
    
    public float getSegPctCompletion() {
        return (((float)(currSegNumber-1))/numSegments)*100 ;
    }
    
    public float getCurSegPctCompletion() {
        return (((float)(currSegCompletion))/currSegSize)*100 ;
    }
}
