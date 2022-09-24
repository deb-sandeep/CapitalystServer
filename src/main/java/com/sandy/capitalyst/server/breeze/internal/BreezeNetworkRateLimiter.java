package com.sandy.capitalyst.server.breeze.internal;

import org.apache.log4j.Logger ;

public class BreezeNetworkRateLimiter {

    private static final Logger log = Logger.getLogger( BreezeNetworkRateLimiter.class ) ;
    
    // Per minute threshold
    // Per day threshold

    public int getDelay( String userId ) {
        return 0 ;
    }
}
