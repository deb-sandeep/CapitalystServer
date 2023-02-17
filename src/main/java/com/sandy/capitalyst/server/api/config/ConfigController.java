package com.sandy.capitalyst.server.api.config;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

@RestController
public class ConfigController {

    private static final Logger log = Logger.getLogger( ConfigController.class ) ;
    
    @Autowired
    private NVPRepo nvpRepo = null ;
    
    @GetMapping( "/AllConfig" ) 
    public ResponseEntity<Map<String, List<NVPVO>>> getRefData() {
        
        try {
            Map<String, List<NVPVO>> cfgMap = new TreeMap<>() ;
            
            for( NVP nvp : nvpRepo.findAll() ) {
                
                String groupName = nvp.getGroupName() ;
                List<NVPVO> groupedCfgs = cfgMap.get( groupName ) ;
                
                if( groupedCfgs == null ) {
                    groupedCfgs = new ArrayList<>() ;
                    cfgMap.put( groupName, groupedCfgs ) ;
                }
                groupedCfgs.add( new NVPVO( nvp ) ) ;
            }
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( cfgMap ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting reference data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }

    @PostMapping( "/Config" ) 
    public ResponseEntity<APIMsgResponse> execute( @RequestBody NVPVO nvpVo ) {
        
        try {
            NVP nvp = nvpRepo.findById( nvpVo.getId() ).get() ;
            
            // This will ensure that if we don't have a config, we are not
            // creating one by accident.
            if( nvp != null ) {
                nvp.inherit( nvpVo ) ;
                nvpRepo.save( nvp ) ;
                
                return ResponseEntity.status( HttpStatus.OK )
                                     .body( APIMsgResponse.SUCCESS ) ;
            }

            return ResponseEntity.status( HttpStatus.BAD_REQUEST )
                                 .body( new APIMsgResponse( "Config doesn't exist." ) ) ;
            
        }
        catch( Exception e ) {
            log.error( "Error :: Saving nvp.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( e.getMessage() ) ) ;
        }
    }
}
