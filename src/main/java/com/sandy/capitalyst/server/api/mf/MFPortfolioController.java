package com.sandy.capitalyst.server.api.mf;

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
import com.sandy.capitalyst.server.dao.mf.MutualFundAsset ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAssetRepo ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxn ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxnRepo ;

@RestController
public class MFPortfolioController {

    private static final Logger log = Logger.getLogger( MFPortfolioController.class ) ;
    
    @Autowired
    private MutualFundAssetRepo mfAssetRepo = null ;
    
    @Autowired
    private MutualFundTxnRepo mfTxnRepo = null ;
    
    @PostMapping( "/MutualFund/Portfolio" ) 
    public ResponseEntity<APIResponse> updateMutualFundPortfolio(
                                @RequestBody List<MutualFundAsset> mfAssets ) {
        try {
            log.debug( "Updating MF portfolio" ) ;
            for( MutualFundAsset asset : mfAssets ) {
                log.debug( asset ) ;
                updateAsset( asset ) ;
            }
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( new APIResponse( "Success" ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Saving account data.", e ) ;
            String stackTrace = ExceptionUtils.getFullStackTrace( e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( new APIResponse( stackTrace ) ) ;
        }
    }
    
    private void updateAsset( MutualFundAsset postedAsset )
        throws Exception {
        
        MutualFundAsset existingAsset = mfAssetRepo.findByOwnerNameAndScheme( 
                                                postedAsset.getOwnerName(), 
                                                postedAsset.getScheme() ) ;
        
        if( existingAsset == null ) {
            log.debug( "No existing asset found. Creating New." ) ;
            mfAssetRepo.save( postedAsset ) ;
        }
        else {
            log.debug( "Updating asset with posted values." ) ;
            existingAsset.setUnitsHeld( postedAsset.getUnitsHeld() ) ;
            existingAsset.setAvgCostPrice( postedAsset.getAvgCostPrice() ) ;
            existingAsset.setValueAtCost( postedAsset.getValueAtCost() ) ;
            existingAsset.setLastRecordedNav( postedAsset.getLastRecordedNav() ) ;
            existingAsset.setValueAtNav( postedAsset.getValueAtNav() ) ;
            existingAsset.setProfitLossAmt( postedAsset.getProfitLossAmt() ) ;
            existingAsset.setProfitLossPct( postedAsset.getProfitLossPct() ) ;
            mfAssetRepo.save( existingAsset ) ;
        }
    }

    @PostMapping( "/MutualFund/TxnList" ) 
    public ResponseEntity<APIResponse> updateMutualFundTxns(
                                @RequestBody List<MFTxn> txnList ) {
        try {
            log.debug( "Updating MF transactions" ) ;
            int numTxnSaved = 0 ;
            for( MFTxn txn : txnList ) {
                if( saveTxn( txn ) ) {
                    numTxnSaved++ ;
                }
            }
            String msg = "Success. Num txn saved = " + numTxnSaved ;
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
    
    private boolean saveTxn( MFTxn postedTxn )
        throws Exception {
        
        MutualFundAsset mf = mfAssetRepo.findByOwnerNameAndScheme( 
                                                    postedTxn.getOwnerName(), 
                                                    postedTxn.getScheme() ) ;
        MutualFundTxn existingTxn = mfTxnRepo.findByMfIdAndTxnDate( 
                                                    mf.getId(), 
                                                    postedTxn.getTxnDate() ) ;
        
        if( existingTxn == null ) {
            
            MutualFundTxn mfTxn = new MutualFundTxn() ;
            mfTxn.setMfId( mf.getId() ) ;
            mfTxn.setTxnDate( postedTxn.getTxnDate() ) ;
            mfTxn.setTxnType( postedTxn.getTxnType() ) ;
            mfTxn.setTxnChannel( postedTxn.getTxnChannel() ) ;
            mfTxn.setNavPerUnit( postedTxn.getNavPerUnit() ) ;
            mfTxn.setNumUnits( postedTxn.getNumUnits() ) ;
            mfTxn.setAmount( postedTxn.getAmount() ) ;
            
            mfTxnRepo.save( mfTxn ) ;
            return true ;
        }
        return false ;
    }
}
