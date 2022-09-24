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

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

@RestController
public class ConfigController {

    private static final Logger log = Logger.getLogger( ConfigController.class ) ;
    
    private static final String NO_GROUP = "-- NO GROUP --" ;

    @Autowired
    private NVPRepo nvpRepo = null ;
    
    @GetMapping( "/AllConfig" ) 
    public ResponseEntity<Map<String, List<NVP>>> getRefData() {
        try {
            Map<String, List<NVP>> cfgMap = new TreeMap<>() ;
            cfgMap.put( NO_GROUP, new ArrayList<>() ) ;
            
            for( NVP nvp : nvpRepo.findAll() ) {
                String groupName = nvp.getGroupName() == null ? 
                                   NO_GROUP : nvp.getGroupName() ;
                
                List<NVP> groupedCfgs = cfgMap.get( groupName ) ;
                if( groupedCfgs == null ) {
                    groupedCfgs = new ArrayList<>() ;
                    cfgMap.put( groupName, groupedCfgs ) ;
                }
                groupedCfgs.add( nvp ) ;
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
    public ResponseEntity<APIResponse> execute( @RequestBody NVPVO nvpVo ) {
        try {
            log.debug( "Saving " + nvpVo ) ;
            
            NVP nvp = nvpRepo.findById( nvpVo.getId() ).get() ;
            nvp.inherit( nvpVo ) ;
            
            // Note: We don't need to return the saved NVP because we are
            // not letting the user create a new NVP, so the client does 
            // not need the autogeneraed ID information.
            nvpRepo.save( nvp ) ;
            
            NVPManager.instance().notifyConfigChange( nvp ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving nvp.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( e.getMessage() ) ) ;
        }
    }
}
