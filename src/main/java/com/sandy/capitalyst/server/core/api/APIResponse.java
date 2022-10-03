package com.sandy.capitalyst.server.core.api;

import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;

import static org.springframework.http.HttpStatus.* ;

public class APIResponse {
    
    public static APIResponse SUCCESS = new APIResponse( "Success" ) ;

    private String message = null ;
    
    public static ResponseEntity<APIResponse> serverError( String msg ) {
        return ResponseEntity.status( INTERNAL_SERVER_ERROR )
                             .body( new APIResponse( msg ) ) ;
    }
    
    public static ResponseEntity<APIResponse> success() {
        return ResponseEntity.status( HttpStatus.OK )
                             .body( APIResponse.SUCCESS ) ;
    }
    
    public static ResponseEntity<APIResponse> success( String msg ) {
        return ResponseEntity.status( HttpStatus.OK )
                             .body( new APIResponse( msg ) ) ;
    }
    
    public APIResponse() {
        this.message = "OK" ;
    }
    
    public APIResponse( String msg ) {
        this.message = msg ;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }
}
