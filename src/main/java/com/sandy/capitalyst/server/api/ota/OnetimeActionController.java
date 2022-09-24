package com.sandy.capitalyst.server.api.ota;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.log4j.Logger ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PathVariable ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.api.ota.action.emrefresh.EquityMasterRefreshOTA ;
import com.sandy.capitalyst.server.api.ota.action.idirectmap.IDirectSymbolMappingOTA ;
import com.sandy.capitalyst.server.api.ota.action.idxrefresh.IndexMasterRefreshOTA ;
import com.sandy.capitalyst.server.api.ota.action.prevcloseupdater.HistPrevCloseUpdater ;
import com.sandy.capitalyst.server.api.ota.action.tradeupdater.EquityTradeUpdater ;
import com.sandy.capitalyst.server.api.ota.vo.PartResult ;
import com.sandy.capitalyst.server.core.api.APIResponse ;

@RestController
public class OnetimeActionController {

    private static final Logger log = Logger.getLogger( OnetimeActionController.class ) ;
    
    private Map<String, Class<? extends OTA>> otaMap = new HashMap<>() ;
    private Map<String, OTA> executingActions = new HashMap<>() ;

    private ExecutorService executorService = null ;
    
    public OnetimeActionController() {
        
        otaMap.put( EquityTradeUpdater.NAME,      EquityTradeUpdater.class      ) ;
        otaMap.put( HistPrevCloseUpdater.NAME,    HistPrevCloseUpdater.class    ) ;
        otaMap.put( IndexMasterRefreshOTA.NAME,   IndexMasterRefreshOTA.class   ) ;
        otaMap.put( EquityMasterRefreshOTA.NAME,  EquityMasterRefreshOTA.class  ) ;
        otaMap.put( IDirectSymbolMappingOTA.NAME, IDirectSymbolMappingOTA.class ) ;
        
        executorService = Executors.newFixedThreadPool( 5 ) ;
    }
    
    @PostMapping( "/OTA/Execute/{actionId}" ) 
    public ResponseEntity<APIResponse> execute( 
                                @PathVariable String actionId,
                                @RequestBody Map<String, String> parameters ) {
        try {
            log.debug( "Executing action " + actionId ) ;
            
            Class<? extends OTA> clz = null ;
            OTA action = null ;
            
            clz = otaMap.get( actionId ) ;
            if( clz == null ) {
                throw new IllegalArgumentException( "Invalid OTA - " + actionId ) ;
            }
            
            action = clz.getDeclaredConstructor().newInstance() ;
            action.setParameters( parameters ) ;
            
            executorService.execute( action ) ;
            executingActions.put( actionId, action ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( e.getMessage() ) ) ;
        }
    }
    
    @GetMapping( "/OTA/PartResults" )
    public ResponseEntity<Map<String, List<PartResult>>> getResults() {
        try {
            Map<String, List<PartResult>> results = new HashMap<>() ;
            Iterator<Map.Entry<String, OTA>> iter = null ; 
                    
            iter = executingActions.entrySet().iterator() ;
            while( iter.hasNext() ) {
                Entry<String, OTA> entry = iter.next() ;
                
                String actionId = entry.getKey() ;
                OTA    action   = entry.getValue() ;
                
                List<PartResult> actionResults = action.getPartResults() ;
                if( actionResults != null ) {
                    results.put( actionId, actionResults ) ;
                }
                
                if( action.isComplete() ) {
                    iter.remove() ;
                }
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
