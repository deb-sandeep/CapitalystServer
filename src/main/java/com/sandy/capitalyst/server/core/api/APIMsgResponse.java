package com.sandy.capitalyst.server.core.api;

import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;

import static org.springframework.http.HttpStatus.* ;

public class APIMsgResponse extends APIMapResponse {
    
    public static APIMsgResponse SUCCESS = new APIMsgResponse( true, "Success" ) ;

    public static ResponseEntity<APIMsgResponse> serverError( String msg ) {
        return ResponseEntity.status( INTERNAL_SERVER_ERROR )
                             .body( new APIMsgResponse( false, msg ) ) ;
    }
    
    public static ResponseEntity<APIMsgResponse> success() {
        return ResponseEntity.status( HttpStatus.OK )
                             .body( APIMsgResponse.SUCCESS ) ;
    }
    
    public static ResponseEntity<APIMsgResponse> success( String msg ) {
        return ResponseEntity.status( HttpStatus.OK )
                             .body( new APIMsgResponse( true, msg ) ) ;
    }
    
    public APIMsgResponse() {
        this( true, "Success" ) ;
    }
    
    public APIMsgResponse( String msg ) {
        this( true, msg ) ;
    }
    
    public APIMsgResponse( boolean success, String msg ) {
        super.set( "message", msg ) ;
        super.setResult( success ? "Success" : "Failure" ) ;
    }
}
