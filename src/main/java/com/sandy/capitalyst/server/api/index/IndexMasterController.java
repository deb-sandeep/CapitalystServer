package com.sandy.capitalyst.server.api.index ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Optional ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.DeleteMapping ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.index.helper.IndexRefresher ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.IndexMasterRepo ;

@RestController
public class IndexMasterController {

    private static final Logger log = Logger.getLogger( IndexMasterController.class ) ;
    
    @Autowired
    private IndexMasterRepo imRepo = null ;
    
    @GetMapping( "/IndexMaster" ) 
    public ResponseEntity<List<IndexMaster>> getAll() {
        
        log.debug( "Getting index masters" ) ;
        List<IndexMaster> masters = new ArrayList<>() ;
        
        try {
            for( IndexMaster master : imRepo.findAll() ) {
                masters.add( master ) ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( masters ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting index masters.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @GetMapping( "/IndexMaster/{id}" ) 
    public ResponseEntity<IndexMaster> get( @PathVariable Integer id ) {
        
        log.debug( "Getting index master for id = " + id ) ;
        
        try {
            IndexMaster master = null ;
            Optional<IndexMaster> result = imRepo.findById( id ) ;
            if( result.isPresent() ) {
                master = result.get() ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( master ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting index masters.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/IndexMaster" ) 
    public ResponseEntity<IndexMaster> saveOrUpdate(
                                             @RequestBody IndexMaster master ) {
        try {
            log.debug( "Updating index master" ) ;
            master = imRepo.save( master ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( master ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving index master.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/IndexMaster/Refresh/{id}" ) 
    public ResponseEntity<APIMsgResponse> refreshIdx( @PathVariable Integer id ) {
        try {
            log.debug( "Updating index master" ) ;
            
            IndexRefresher refresher = new IndexRefresher( null ) ;
            int numUpdates = refresher.refreshIndex( id ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse(
                                         numUpdates + " symbols updated." ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving index master.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( e.getMessage() ) ) ;
        }
    }

    @DeleteMapping( "/IndexMaster/{id}" ) 
    public ResponseEntity<APIMsgResponse> delete( @PathVariable Integer id ) {
        try {
            log.debug( "Deleting index master. " + id ) ;
            imRepo.deleteById( id ) ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse( "Successfully deleted" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( e.getMessage() ) ) ;
        }
    }
}
