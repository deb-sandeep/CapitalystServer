package com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal;

import java.util.LinkedList ;
import java.util.Map ;
import java.util.Queue ;
import java.util.concurrent.ConcurrentHashMap ;

public class EventRateMonitor {
    
    private static final String DEF_STREAM_ID = "__DEF_STREAM_ID__" ;
    
    private int windowDurationSec = 120 ;
    private int eventThreshold = 10 ;
    
    private Map<String, Queue<Long>> eventTimelines = new ConcurrentHashMap<>() ;
    
    public EventRateMonitor( int windowDurationSec, int eventThreshold ) {
        
        this.windowDurationSec = windowDurationSec ;
        this.eventThreshold = eventThreshold ;
    }
    
    public boolean registerEvent() {
        return this.registerEvent( null ) ;
    }
    
    public boolean registerEvent( String streamId ) {
        
        streamId = (streamId == null) ? DEF_STREAM_ID : streamId ;
        
        Queue<Long> eventTimelineQueue = getEventTimelineQueue( streamId ) ;
        eventTimelineQueue.add( System.currentTimeMillis() ) ;
        
        return hasThresholdBreached( streamId ) ;
    }
    
    public boolean hasThresholdBreached() {
        return hasThresholdBreached( null ) ;
    }
    
    public boolean hasThresholdBreached( String streamId ) {
        
        streamId = (streamId == null) ? DEF_STREAM_ID : streamId ;

        long windowEnd   = System.currentTimeMillis() ;
        long windowStart = windowEnd - (windowDurationSec*1000) ;

        Queue<Long> eventTimelineQueue = getEventTimelineQueue( streamId ) ;
        
        cleanEventTimeQueue( eventTimelineQueue, windowStart ) ;

        return eventTimelineQueue.size() >= eventThreshold ;
    }

    private Queue<Long> getEventTimelineQueue( String userId ) {
        
        Queue<Long> callTimelineQueue = null ;
        
        callTimelineQueue = eventTimelines.get( userId ) ;
        if( callTimelineQueue == null ) {
            callTimelineQueue = new LinkedList<>() ;
            eventTimelines.put( userId, callTimelineQueue ) ;
        }
        
        return callTimelineQueue ;
    }
    
    private void cleanEventTimeQueue( Queue<Long> queue, long windowStart ) {
        
        while( !queue.isEmpty() ) {
            if( queue.peek() < windowStart ) {
                queue.remove() ;
            }
            else {
                break ;
            }
        }
    }
}
