package com.sandy.capitalyst.server.job.equity.idirectmap;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.List ;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.job.equity.idirectmap.IDirectSecurityMaprDownloader.SecMapping ;

@DisallowConcurrentExecution
public class ICICIDirectSymbolMappingJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( ICICIDirectSymbolMappingJob.class ) ;
    
    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "ICICIDirectSymbolMappingJob executeJob" ) ;
        mapICICIDirectSymbols( null ) ;
    }
    
    public void mapICICIDirectSymbols( OTA ota ) throws Exception {
        
        List<SecMapping> mappings = null ;
        IDirectSecurityMaprDownloader downloader = null ;
        
        EquityMasterRepo emRepo = null ;
        EquityHoldingRepo ehRepo = null ;
        
        emRepo     = getBean( EquityMasterRepo.class ) ;
        ehRepo     = getBean( EquityHoldingRepo.class ) ;
        
        downloader = new IDirectSecurityMaprDownloader() ;
        mappings   = downloader.downloadSecurityMappings() ;
        
        if( ota != null ) {
            ota.addResult( mappings.size() + " mappings found." ) ;
        }
        
        int numEquityMastersUpdated = 0 ;
        int numHoldingsUpdated = 0 ;
        
        for( SecMapping map : mappings ) {
            
            EquityMaster em = emRepo.findByIsin( map.getIsin() ) ;
            
            if( em != null ) {
                if( StringUtil.isEmptyOrNull( em.getSymbolIcici() ) || 
                    !em.getSymbolIcici().equals( map.getSymbolICICI() ) ) {
                    
                    em.setSymbolIcici( map.getSymbolICICI() ) ;
                    emRepo.save( em ) ;
                    numEquityMastersUpdated++ ;
                    
                    String msg = "EquityMaster updated with " + map.getSymbolICICI() ;
                    
                    log.debug( msg ) ;
                    if( ota != null ) {
                        ota.addResult( msg ) ;
                    }
                }
            }
            
            List<EquityHolding> holdings = ehRepo.findByIsin( map.getIsin() ) ;
            if( holdings != null ) {
                for( EquityHolding holding : holdings ) {
                    
                    if( StringUtil.isEmptyOrNull( holding.getSymbolIcici() ) || 
                        !holding.getSymbolIcici().equals( map.getSymbolICICI() ) ) {
                        
                        holding.setSymbolIcici( map.getSymbolICICI() ) ;
                        ehRepo.save( holding ) ;
                        numHoldingsUpdated++ ;
                        
                        String msg = "Holding " + holding.getId() + 
                                     " updated with " + map.getSymbolICICI() ;
                        
                        log.debug( msg ) ;
                        if( ota != null ) {
                            ota.addResult( msg ) ;
                        }
                    }
                }
            }
        }
        
        log.debug( "Num equity masters updated - " + numEquityMastersUpdated ) ;
        log.debug( "Num holdings updated - " + numHoldingsUpdated ) ;
        
        if( ota != null ) {
            ota.addResult( "Num equity masters updated - " + numEquityMastersUpdated );
            ota.addResult( "Num holdings updated - " + numHoldingsUpdated );
        }
    }
}