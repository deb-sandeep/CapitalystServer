package com.sandy.capitalyst.server.api.mf;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.mf.MutualFundMaster ;
import com.sandy.capitalyst.server.dao.mf.repo.MutualFundRepo ;

@RestController
public class EnrichMFMetaController {

    private static final Logger log = Logger.getLogger( EnrichMFMetaController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yy" ) ;
    
    @Autowired
    private MutualFundRepo mfRepo = null ;
    
    @PostMapping( "/MutualFund/EnrichMFMeta" ) 
    public ResponseEntity<APIResponse> refreshMFMeta(
                                @RequestBody List<String[]> mfMetaDetails ) {
        try {
            log.debug( "Uploading Mutual Fund meta" ) ;
            
            int numRecordsUpdated = 0 ;
            
            for( String[] record : mfMetaDetails ) {

                String groupId   = record[0].trim() ;
                String coName    = record[1].trim() ;
                String isin      = record[2].trim() ;
                String fundName  = record[3].trim() ;
                String category  = record[4].trim() ;
                String distType  = record[5].trim() ;
                float  nav       = 0.0F ;
                Date   navDate   = null ;
                
                if( record[6] != null ) {
                    nav = Float.parseFloat( record[6].trim() ) ;
                }
                if( record[7] != null ) {
                    navDate = SDF.parse( record[7].trim() ) ;
                }
                
                MutualFundMaster mf = mfRepo.findByIsin( isin ) ;
                boolean recordDirty = false ;
                
                if( mf == null ) {
                    mf = new MutualFundMaster() ;
                    mf.setFundGroupId( groupId ) ;
                    mf.setFundMgmtCoName( coName ) ;
                    mf.setIsin( isin ) ;
                    mf.setFundName( fundName ) ;
                    mf.setCategory( category ) ;
                    mf.setDistributionType( distType ) ;
                    mf.setLatestNav( nav ) ;
                    mf.setLastUpdate( navDate ) ;
                    recordDirty = true ;
                }
                else {
                    if( navDate != null && nav != 0.0 ) {
                        if( mf.getLastUpdate() == null ) {
                            mf.setLatestNav( nav ) ;
                            mf.setLastUpdate( navDate ) ;
                            recordDirty = true ;
                        }
                        else if( mf.getLastUpdate().before( navDate ) ) {
                            mf.setLatestNav( nav ) ;
                            mf.setLastUpdate( navDate ) ;
                            recordDirty = true ;
                        }
                    }
                }
                
                if( recordDirty ) {
                    mfRepo.save( mf ) ;
                    numRecordsUpdated++ ;
                }
            }
            
            String msg = "Success. Num records updated = " + 
                         numRecordsUpdated ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            String stackTrace = ExceptionUtils.getFullStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( stackTrace ) ) ;
        }
    }
}
