package com.sandy.capitalyst.server.breeze.internal;

import java.util.HashMap ;
import java.util.LinkedList ;
import java.util.Map ;
import java.util.Queue ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;

public class BreezeNetworkRateLimiter {

    private static final Logger log = Logger.getLogger( BreezeNetworkRateLimiter.class ) ;
    
    private static final long WINDOW_SIZE_SECS = 60 ;
    private static final long WINDOW_DURATION  = WINDOW_SIZE_SECS*1000 ;
    
    private Map<String, Queue<Long>> callTimelines = new HashMap<>() ;
    
    public void throttle( String userId ) {
        
        int rateLimit = Breeze.instance().getNVPCfg().getRateLimitPerMinute() ;
        
        Queue<Long> callTimelineQueue = getCallTimelineQueue( userId ) ;
        callTimelineQueue.add( System.currentTimeMillis() ) ;
        
        long windowEnd   = System.currentTimeMillis() ;
        long windowStart = windowEnd - WINDOW_DURATION ;

        cleanCallTimeQueue( callTimelineQueue, windowStart ) ;
        
        while( callTimelineQueue.size() > rateLimit ) {
            
            try {
                TimeUnit.MILLISECONDS.sleep( 200 ) ;
                log.debug( "  Throttling. Rate " + callTimelineQueue.size() ) ;
            }
            catch( InterruptedException e ) {}
            
            windowEnd   = System.currentTimeMillis() ;
            windowStart = windowEnd - WINDOW_DURATION ;

            cleanCallTimeQueue( callTimelineQueue, windowStart ) ;
        }
    }

    private Queue<Long> getCallTimelineQueue( String userId ) {
        
        Queue<Long> callTimelineQueue = null ;
        
        callTimelineQueue = callTimelines.get( userId ) ;
        if( callTimelineQueue == null ) {
            callTimelineQueue = new LinkedList<>() ;
            callTimelines.put( userId, callTimelineQueue ) ;
        }
        
        return callTimelineQueue ;
    }
    
    private void cleanCallTimeQueue( Queue<Long> queue, long windowStart ) {
        
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
