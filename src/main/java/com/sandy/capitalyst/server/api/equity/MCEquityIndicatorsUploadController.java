package com.sandy.capitalyst.server.api.equity ;

import java.util.Date ;

import org.apache.commons.lang3.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.StockIndicators ;
import com.sandy.capitalyst.server.api.equity.vo.StockIndicators.TechIndicator ;
import com.sandy.capitalyst.server.core.api.APIMsgResponse ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicatorsHist ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsHistRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTechIndicatorRepo ;

// @Post - /Equity/Master/MCStockIndicators

@RestController
public class MCEquityIndicatorsUploadController {

    private static final Logger log = Logger.getLogger( MCEquityIndicatorsUploadController.class ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private EquityIndicatorsRepo eiRepo = null ;
    
    @Autowired
    private EquityTechIndicatorRepo etiRepo = null ;
    
    @Autowired
    private EquityIndicatorsHistRepo eihRepo = null ;
    
    @PostMapping( "/Equity/Master/MCStockIndicators" ) 
    public ResponseEntity<APIMsgResponse> updateMCStockIndicators(
                                     @RequestBody StockIndicators ind ) {
        try {
            log.debug( "Updating indicators for " + ind.getSymbolNse() ) ;
            
            saveIndicators( ind ) ;
            
            RecoManager.instance().setEquityDataUpdated( true ) ;

            String msg = "Success. Updated indicators for " + ind.getSymbolNse() ;
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIMsgResponse( msg ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving equity holding.", e ) ;
            String stackTrace = ExceptionUtils.getStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIMsgResponse( stackTrace ) ) ;
        }
    }
    
    private void saveIndicators( StockIndicators ind )
        throws Exception {
        
        EquityMaster em = emRepo.findByIsin( ind.getIsin() ) ;
        
        if( em == null ) {
            throw new Exception( "Equity " + ind.getSymbolNse() + " not found." ) ;
        }
        
        if( StringUtil.isNotEmptyOrNull( ind.getSector() ) ) {
            em.setSector( ind.getSector().trim() ) ;
            emRepo.save( em ) ;
        }
        
        EquityIndicators eiDao = null ;
        EquityTechIndicator etiDao = null ;
        EquityIndicatorsHist eihDao = null ;
        
        eiDao = eiRepo.findByIsin( ind.getIsin() ) ;
        if( eiDao == null ) {
            eiDao = new EquityIndicators( ind ) ;
        }
        else {
            String prevTrend = eiDao.getTrend() ;
            Date   prevEntryDate = eiDao.getAsOnDate() ;
            Date   curEntryDate  = ind.getAsOnDate() ;
            
            eiDao.copy( ind ) ;
            
            if( prevEntryDate == null || curEntryDate.after( prevEntryDate ) ) {
                eiDao.setPrevTrend( prevTrend ) ;
            }
        }
        
        eihDao = eihRepo.findByIsinAndAsOnDate( ind.getIsin(), eiDao.getAsOnDate() ) ;
        if( eihDao == null ) {
            eihDao = new EquityIndicatorsHist( eiDao ) ;
        }
        else {
            eihDao.copy( eiDao ) ;
        }
        
        eiRepo.save( eiDao ) ;
        eihRepo.save( eihDao ) ;
        
        for( TechIndicator ti : ind.getIndicators() ) {
            etiDao = etiRepo.findByIsinAndName( ind.getIsin(), ti.getName() ) ;
            if( etiDao == null ) {
                etiDao = new EquityTechIndicator( ind, ti ) ;
            }
            else {
                etiDao.copy( ind, ti ) ;
            }
            etiRepo.save( etiDao ) ;
        }
    }
}
