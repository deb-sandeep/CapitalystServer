package com.sandy.capitalyst.server.api.account;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RequestMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;

@RestController
@RequestMapping( "/Account" )
public class AccountRestController {

    private static final Logger log = Logger.getLogger( AccountRestController.class ) ;
    
    @Autowired
    private AccountRepo aiRepo = null ;
    
    @GetMapping( "/SavingAccount" ) 
    public ResponseEntity<List<Account>> getAccountSummaries() {
        try {
            List<Account> accounts = new ArrayList<>() ;
            Iterable<Account> source = aiRepo.findAll() ;
            source.forEach( accounts::add ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( accounts ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting account summaries.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/SavingAccount" ) 
    public ResponseEntity<Account> saveAccount( @RequestBody Account input ) {
        try {
            log.debug( "Saving account data." ) ;
            Account result = aiRepo.save( input ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( result ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @DeleteMapping( "/SavingAccount/{id}" ) 
    public ResponseEntity<APIResponse> deleteAccount( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting account. " + id ) ;
            aiRepo.deleteById( id ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
