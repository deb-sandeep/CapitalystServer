package com.sandy.capitalyst.server.api.account;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;
import org.springframework.web.multipart.MultipartFile ;

@RestController
public class AccountStmtUploadRestController {

    private static final Logger log = Logger.getLogger( AccountStmtUploadRestController.class ) ;
    
    @PostMapping( "/Account/Statement/Upload" ) 
    public ResponseEntity<List<StmtUploadResult>> uploadAccountStatements( 
            @RequestParam( "files" ) MultipartFile[] files,
            @RequestParam( "accountId" ) Integer accountId ) {
        try {
            List<StmtUploadResult> results = new ArrayList<>() ;
            log.debug( "Saving uploaded statements. " ) ;
            log.debug( "Account id = " + accountId ) ;
            log.debug( "Number of files = " + files.length ) ;
            for( MultipartFile file : files ) {
                log.debug( "File -->\n--------------------------------------" ) ;
                log.debug( "\t " + file.getOriginalFilename() ) ;
                log.debug( "\t " + file.getSize() ) ;
                log.debug( "\t " + new String( file.getBytes() ) ) ;
                log.debug( "\n\n" );
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( results ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
