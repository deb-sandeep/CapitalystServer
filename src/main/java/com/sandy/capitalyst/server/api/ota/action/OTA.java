package com.sandy.capitalyst.server.api.ota.action;

import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.EndOfProcessing ;
import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.Exception ;
import static com.sandy.capitalyst.server.api.ota.vo.PartResult.ResultType.Message ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.LinkedBlockingQueue ;

import org.apache.log4j.AppenderSkeleton ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.apache.log4j.spi.LoggingEvent ;

import com.sandy.capitalyst.server.api.ota.vo.PartResult ;
import com.sandy.capitalyst.server.core.log.CapitalystOTALogLayout ;

import lombok.Getter ;
import lombok.Setter ;

public abstract class OTA implements Runnable, OTALogger {
    
    private static final Logger log = Logger.getLogger( OTA.class ) ;
    
    public class OTALogAppender extends AppenderSkeleton {
        
        private static final String PATTERN = "[indent]%m%n";

        public OTALogAppender() {
            super() ;
            
            CapitalystOTALogLayout layout = new CapitalystOTALogLayout() ;
            layout.setConversionPattern( PATTERN ) ;
            
            setLayout( layout ) ;
            setThreshold( Level.INFO ) ;
            activateOptions() ;
        }

        @Override
        protected void append( LoggingEvent event ) {
            
            String msg = layout.format( event ) ;
            if( msg.endsWith( "\n" ) ) {
                msg = msg.substring( 0, msg.length()-1 ) ;
            }
            addResult( msg ) ;
        }

        @Override
        public void close() {}

        @Override
        public boolean requiresLayout() { return true; }
    }    

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

        OTALogAppender appender = new OTALogAppender() ;
        Logger.getRootLogger().addAppender( appender ) ;
        
        try {
            log.info( "!- Executing OTA - " + this.name ) ;
            execute() ;
        }
        catch( Exception e ) {
            addResult( e ) ;
            log.error( "- Error in OTA - ", e ) ;
        }
        finally {
            log.info( "<< ENDED." );
            markEndOfProcessing() ;
            Logger.getRootLogger().removeAppender( appender ) ;
        }
    }
    
    protected abstract void execute() throws Exception ;
    
    public void addResult( String message ) {
        queue.add( new PartResult( Message, message ) ) ;
    }
    
    public void addResult( Exception e ) {
        queue.add( new PartResult( Exception, e.getMessage() ) ) ;
    }
    
    private void markEndOfProcessing() {
        queue.add( new PartResult( EndOfProcessing, null ) ) ;
        this.complete = true ;
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
