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
import com.sandy.capitalyst.server.api.ota.action.eqhistupdate.HistoricEQDataImporterOTA ;
import com.sandy.capitalyst.server.api.ota.action.idcompressor.IDCompressorOTA ;
import com.sandy.capitalyst.server.api.ota.action.idirectmap.IDirectSymbolMappingOTA ;
import com.sandy.capitalyst.server.api.ota.action.idxhistupdate.HistoricIdxDataImporterOTA ;
import com.sandy.capitalyst.server.api.ota.action.idxrefresh.IndexMasterRefreshOTA ;
import com.sandy.capitalyst.server.api.ota.action.prevcloseupdater.HistPrevCloseUpdater ;
import com.sandy.capitalyst.server.api.ota.action.tradeupdater.EquityTradeUpdater ;
import com.sandy.capitalyst.server.api.ota.vo.PartResult ;
import com.sandy.capitalyst.server.core.api.APIMapResponse ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;

@RestController
public class OnetimeActionController {

    private static final Logger log = Logger.getLogger( OnetimeActionController.class ) ;
    
    private Map<String, Class<? extends OTA>> otaMap = new HashMap<>() ;
    private Map<String, OTA> executingActions = new HashMap<>() ;

    private ExecutorService executorService = null ;
    
    public OnetimeActionController() {
        
        otaMap.put( EquityTradeUpdater.NAME,        EquityTradeUpdater.class      ) ;
        otaMap.put( HistPrevCloseUpdater.NAME,      HistPrevCloseUpdater.class    ) ;
        otaMap.put( IndexMasterRefreshOTA.NAME,     IndexMasterRefreshOTA.class   ) ;
        otaMap.put( EquityMasterRefreshOTA.NAME,    EquityMasterRefreshOTA.class  ) ;
        otaMap.put( IDirectSymbolMappingOTA.NAME,   IDirectSymbolMappingOTA.class ) ;
        otaMap.put( HistoricEQDataImporterOTA.NAME, HistoricEQDataImporterOTA.class ) ;
        otaMap.put( HistoricIdxDataImporterOTA.NAME,HistoricIdxDataImporterOTA.class ) ;
        otaMap.put( IDCompressorOTA.NAME,           IDCompressorOTA.class ) ;
        
        executorService = Executors.newFixedThreadPool( 5 ) ;
    }
    
    private OTA getOTAInstance( String actionId ) 
        throws Exception {
        
        Class<? extends OTA> clz = null ;
        OTA action = null ;
        
        clz = otaMap.get( actionId ) ;
        if( clz == null ) {
            throw new IllegalArgumentException( "Invalid OTA - " + actionId ) ;
        }
        
        action = clz.getDeclaredConstructor().newInstance() ;
        return action ;
    }
        
    @GetMapping( "/OTA/DefaultConfigs" ) 
    public ResponseEntity<? extends APIMapResponse> getDefaultConfigs() {
        
        try {
            log.debug( "Getting default configuration for all action " ) ;
            
            APIMapResponse response = new APIMapResponse() ;
            
            otaMap.forEach( (k,v) -> {
                try {
                    OTA instance = getOTAInstance( k ) ;
                    Map<String, Object> params = instance.getDefaultParameters() ;
                    response.set( k, params ) ;
                }
                catch( Exception e ) {
                    log.error( "Exception in getting OTA instance.", e ) ;
                }
            } ) ;
            
            return ResponseEntity.status( HttpStatus.OK ).body( response ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return APIMsgResponse.serverError( e.getMessage() ) ;
        }
    }
    
    @PostMapping( "/OTA/Execute/{actionId}" ) 
    public ResponseEntity<APIMsgResponse> execute( 
                                @PathVariable String actionId,
                                @RequestBody Map<String, Object> parameters ) {
        try {
            log.debug( "Executing action " + actionId ) ;
            
            OTA action = getOTAInstance( actionId ) ;
            
            if( parameters != null && !parameters.isEmpty() ) {
                log.debug( "Parameters : " ) ;
                parameters.forEach( (k,v)->{
                    log.debug( "  " + k + " = " + v ) ;
                }) ;
                action.setParameters( parameters ) ;
            }
            
            executorService.execute( action ) ;
            executingActions.put( actionId, action ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( APIMsgResponse.SUCCESS ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( e.getMessage() ) ) ;
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
