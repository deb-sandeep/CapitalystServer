package com.sandy.capitalyst.server.api.job;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.api.APIResponse ;

@RestController
public class JobController {

    private static final Logger log = Logger.getLogger( JobController.class ) ;
    
    @PostMapping( "/Job/TriggerNow/{jobName}" ) 
    public ResponseEntity<APIResponse> saveCashEntry( 
                                     @PathVariable String jobName ) {
        try {
            log.debug( "Triggering job = " + jobName ) ;
            CapitalystServer.getApp()
                            .getScheduler()
                            .triggerJob( jobName ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
