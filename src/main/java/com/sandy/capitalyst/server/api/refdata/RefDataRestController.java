package com.sandy.capitalyst.server.api.refdata;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RestController ;

@RestController
public class RefDataRestController {

    private static final Logger log = Logger.getLogger( RefDataRestController.class ) ;

    @GetMapping( "/RefData" ) 
    public ResponseEntity<RefData> getRefData() {
        try {
            RefData results = RefData.instance() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting reference data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
