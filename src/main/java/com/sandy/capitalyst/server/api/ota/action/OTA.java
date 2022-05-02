package com.sandy.capitalyst.server.api.ota.action;

import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.EndOfProcessing ;
import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.Exception ;
import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.Message ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.LinkedBlockingQueue ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.ota.vo.PartResult ;

import lombok.Getter ;
import lombok.Setter ;

public abstract class OTA implements Runnable {
    
    private static final Logger log = Logger.getLogger( OTA.class ) ;

    @Getter
    private String name = null ;
    
    @Setter
    protected Map<String, String> parameters = null ;
    
    private LinkedBlockingQueue<PartResult> queue = new LinkedBlockingQueue<>() ;
    
    @Getter 
    private boolean complete = false ;
    
    protected OTA( String name ) {
        this.name = name ;
    }
    
    @Override
    public final void run() {

        try {
            log.debug( "Executing OTA - " + this.name ) ;
            execute() ;
        }
        catch( Exception e ) {
            addResult( e ) ;
            log.error( "Error in OTA - ", e ) ;
        }
        finally {
            markEndOfProcessing() ;
        }
    }
    
    protected abstract void execute() throws Exception ;
    
    public void addResult( String message ) {
        queue.add( new PartResult( Message, message ) ) ;
        log.debug( "2CLIENT : " + message ) ;
    }
    
    public void addResult( Exception e ) {
        queue.add( new PartResult( Exception, e.getMessage() ) ) ;
        log.debug( "2CLIENT : Error : " + e.getMessage() ) ;
    }
    
    protected void markEndOfProcessing() {
        queue.add( new PartResult( EndOfProcessing, null ) ) ;
        this.complete = true ;
        log.debug( "2CLIENT : End of processing" ) ;
    }
    
    public List<PartResult> getPartResults() {
        
        List<PartResult> partResults = null ;
        try {
            if( queue.peek() != null ) {
                partResults = new ArrayList<>() ;
                do {
                    partResults.add( queue.take() ) ;
                }
                while( queue.peek() != null ) ;
            }
        }
        catch( InterruptedException e ) {
            log.debug( "Part results aggregation interrupted" ) ;
        }
        
        return partResults ;
    }
}
