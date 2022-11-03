package com.sandy.capitalyst.server.core.asynctask;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import lombok.Data ;

@Data
public class AsyncTaskPartResult {

    private AsyncTaskProgress progress = null ;
    private List<AsyncTaskMessage> messages = new ArrayList<>() ;
    private Map<String, Object> returnValues = new HashMap<>() ;
}
