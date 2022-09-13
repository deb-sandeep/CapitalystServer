package com.sandy.capitalyst.server.breeze.listener;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;

import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener ;
import com.sandy.capitalyst.server.dao.breeze.BreezeInvocationStats ;
import com.sandy.capitalyst.server.dao.breeze.repo.BreezeInvocationStatsRepo ;

public class InvStatsPersistListener
        implements BreezeAPIInvocationListener {

    private BreezeInvocationStatsRepo bisRepo = null ;
    
    public InvStatsPersistListener() {
        bisRepo = getBean( BreezeInvocationStatsRepo.class ) ;
    }

    @Override
    public void preBreezeCall( APIInvocationInfo info ) {}

    @Override
    public void postBreezeCall( APIInvocationInfo info ) {
        
        BreezeInvocationStats stat = null ;
        Date date = info.getCallDatetime() ;
        
        date = DateUtils.truncate( date, Calendar.DAY_OF_MONTH ) ;
        stat = bisRepo.findByDateAndUserNameAndApiId( date, info.getUserName(), info.getApiId() ) ;
        
        if( stat == null ) {
            stat = new BreezeInvocationStats() ;
            stat.setDate( date ) ;
            stat.setApiId( info.getApiId() ) ;
            stat.setUserName( info.getUserName() ) ;
            stat.setNumCalls( 1 ) ;
            stat.setAvgTime( info.getCallDurationInMillis() ) ;
            stat.setMinTime( info.getCallDurationInMillis() ) ;
            stat.setMaxTime( info.getCallDurationInMillis() ) ;
        }
        else {
            
            int numCalls = stat.getNumCalls() ;
            int avgTime  = stat.getAvgTime() ;
            int callDur  = info.getCallDurationInMillis() ;
            
            avgTime = ((numCalls * avgTime) + callDur)/(numCalls + 1) ;
            stat.setAvgTime( avgTime ) ;
            if( callDur < stat.getMinTime() ) {
                stat.setMinTime( callDur ) ;
            }
            if( callDur > stat.getMaxTime() ) {
                stat.setMaxTime( callDur ) ;
            }
            stat.setNumCalls( numCalls+1 ) ;
        }
        
        bisRepo.save( stat ) ;
    }
}
