package com.sandy.capitalyst.server.api.account;

import java.util.List ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RequestMapping ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.dao.fixed_deposit.FixedDeposit ;
import com.sandy.capitalyst.server.dao.fixed_deposit.FixedDepositRepo ;

// Note that a FixedDeposit represents, recurring fixed deposit,
// linked fixed deposit and a normal fixed deposit.

@RestController
@RequestMapping( "/Account" )
public class FixedDepositRestController {

    private static final Logger log = Logger.getLogger( FixedDepositRestController.class ) ;
    
    @Autowired
    private FixedDepositRepo fdRepo = null ;
    
    @GetMapping( "/FixedDeposit" ) 
    public ResponseEntity<List<FixedDeposit>> getAccounts() {
        try {
            List<FixedDeposit> accounts = fdRepo.findAllActiveDeposits() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( accounts ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting account summaries.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/FixedDeposit" ) 
    public ResponseEntity<FixedDeposit> saveAccount( @RequestBody FixedDeposit input ) {
        try {
            log.debug( "Saving account data." ) ;
            input = fdRepo.save( input ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( input ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
}
