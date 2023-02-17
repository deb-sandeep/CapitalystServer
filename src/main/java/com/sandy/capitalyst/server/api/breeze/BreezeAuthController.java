package com.sandy.capitalyst.server.api.breeze;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;

@RestController
public class BreezeAuthController {

    private static final Logger log = Logger.getLogger( BreezeAuthController.class ) ;

    @GetMapping( "/Breeze/Session" ) 
    public ResponseEntity<List<BreezeSession>> getBreezeSessions() {
        
        try {
            List<BreezeSession> sessions = new ArrayList<>() ;
            
            BreezeSessionManager sessionMgr = BreezeSessionManager.instance() ;
            
            Breeze.instance().getAllCreds().forEach( cred -> {
                sessions.add( sessionMgr.getSession( cred ) ) ;
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( sessions ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Creatign budget spread.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/Breeze/APISession/{userId}" ) 
    public ResponseEntity<APIMsgResponse> refreshIdx( 
                    @PathVariable String userId, 
                    @RequestParam( "apisession" ) String sessionId ) {
        
        log.debug( "Received breeze session id" ) ;
        log.debug( "  User ID    = " + userId ) ;
        log.debug( "  Session id = " + sessionId ) ;
        
        try {
            BreezeSessionManager.instance().activateSession( userId, sessionId ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse( "Session activation " + 
                                        "successful for user " + userId ) ) ;
            
        }
        catch( BreezeException e ) {
            
            log.error( "Error :: Activating session for " + userId, e ) ;
            
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( e.toString() ) ) ;
        }
    }
}
