package com.sandy.capitalyst.server.job.mf.eodrefresh;

import java.util.Date ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.mf.HistoricMFData ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAsset ;
import com.sandy.capitalyst.server.dao.mf.repo.HistoricMFDataRepo ;
import com.sandy.capitalyst.server.dao.mf.repo.MutualFundAssetRepo ;
import com.sandy.common.util.StringUtil ;

public class MFEODNAVRefresher {
    
    private static final Logger log = Logger.getLogger( MFEODNAVRefresher.class ) ;
    
    private MutualFundAssetRepo mfAssetRepo = null ;
    private HistoricMFDataRepo histMFNavRepo = null ;
    
    private MFEODParser navParser = new MFEODParser() ;
    private Date latestAvailableNAVDate = null ;
    private Set<String> snappedIsins = new HashSet<>() ;
    
    public MFEODNAVRefresher() {
        mfAssetRepo = CapitalystServer.getBean( MutualFundAssetRepo.class ) ;
        histMFNavRepo = CapitalystServer.getBean( HistoricMFDataRepo.class ) ;
    }

    public Date refreshNAV( Date lastImportDate ) 
            throws Exception {
        
        List<MutualFundAsset> assets = null ;
        
        navParser.downloadMFEODReport() ;
        latestAvailableNAVDate = navParser.getLatestNAVDate() ;
        
        if( lastImportDate != null ) {
            if( !latestAvailableNAVDate.after( lastImportDate ) ) {
                return lastImportDate ;
            }
        }
        
        assets = mfAssetRepo.findByUnitsHeldGreaterThanOrderByOwnerNameAsc( 0 ) ;
        for( MutualFundAsset asset : assets ) {
            refreshAsset( asset ) ;
        }
        
        return latestAvailableNAVDate ;
    }

    private void refreshAsset( MutualFundAsset asset ) {
        
        String isin = asset.getIsin() ;
        if( StringUtil.isEmptyOrNull( isin ) ) {
            log.warn( "ISIN not updated for MF - " + asset.getScheme() ) ;
            return ;
        }
        
        Float latestNav = navParser.getLatestNav( asset.getIsin() ) ;
        if( latestNav == null ) {
            log.warn( "Latest NAV not found for MF - " + asset.getScheme() ) ;
            return ;
        }
        
        log.debug( "Refreshing MF - " + asset.getScheme() ) ;
        asset.setLastRecordedNav( latestNav ) ;
        asset.setLastUpdate( latestAvailableNAVDate ) ;
        computeDerivativeAttributes( asset ) ;
        
        mfAssetRepo.save( asset ) ;
        captureNavSnapshot( asset ) ;
    }

    private void computeDerivativeAttributes( MutualFundAsset asset ) {
        
        asset.setValueAtNav( asset.getUnitsHeld() * asset.getLastRecordedNav() ) ;
        asset.setProfitLossAmt( asset.getValueAtNav() - asset.getValueAtCost() ) ;
        asset.setProfitLossPct( (asset.getProfitLossAmt()/asset.getValueAtCost())*100 );
    }
    
    private void captureNavSnapshot( MutualFundAsset asset ) {
        
        if( snappedIsins.contains( asset.getIsin() ) ) {
            return ;
        }
        
        HistoricMFData navSnapshot = new HistoricMFData() ;
        navSnapshot.setIsin( asset.getIsin() ) ;
        navSnapshot.setDate( asset.getLastUpdate() ) ;
        navSnapshot.setNav( asset.getLastRecordedNav() ) ;
        
        histMFNavRepo.save( navSnapshot ) ;
        snappedIsins.add( asset.getIsin() ) ;
    }
}
