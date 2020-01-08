package com.sandy.capitalyst.server.api.mf;

import java.text.DecimalFormat ;
import java.text.SimpleDateFormat ;
import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.PostMapping ;
import org.springframework.web.bind.annotation.RequestBody ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.mf.helper.MFHolding ;
import com.sandy.capitalyst.server.api.mf.helper.MFPortfolioBuilder ;
import com.sandy.capitalyst.server.api.mf.helper.MFTxn ;
import com.sandy.capitalyst.server.api.mf.helper.MFUpdateInfo ;
import com.sandy.capitalyst.server.core.api.APIResponse ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAsset ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAssetRepo ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxn ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxnRepo ;
import com.sandy.capitalyst.server.util.StringUtil ;

@RestController
public class MFPortfolioController {

    private static final Logger log = Logger.getLogger( MFPortfolioController.class ) ;
    
    private static final DecimalFormat DF = new DecimalFormat( "#.00" ) ;
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yy" ) ;
    
    
    @Autowired
    private MutualFundAssetRepo mfAssetRepo = null ;
    
    @Autowired
    private MutualFundTxnRepo mfTxnRepo = null ;
    
    @GetMapping( "/MutualFund/Portfolio" ) 
    public ResponseEntity<List<MFHolding>> getMutualFundPortfolio() {
        try {
            log.debug( "Getting MF holdings" ) ;
            
            MFPortfolioBuilder builder = new MFPortfolioBuilder() ;
            List<MFHolding> mfHoldings = builder.getMFPortfolio() ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( mfHoldings ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting Mutual Fund portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    @PostMapping( "/MutualFund/Portfolio" ) 
    public ResponseEntity<APIResponse> updateMutualFundPortfolio(
                                @RequestBody List<MutualFundAsset> mfAssets ) {
        try {
            log.debug( "Updating MF portfolio" ) ;
            for( MutualFundAsset asset : mfAssets ) {
                log.debug( asset ) ;
                saveMFAsset( asset ) ;
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
    
    @PostMapping( "/MutualFund/TxnList" ) 
    public ResponseEntity<APIResponse> updateMutualFundTxns(
                                @RequestBody List<MFTxn> txnList ) {
        try {
            log.debug( "Updating MF transactions" ) ;
            int numTxnSaved = 0 ;
            for( MFTxn txn : txnList ) {
                if( saveMFTxn( txn ) ) {
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
    
    @PostMapping( "/MutualFund/InfoUpdate" ) 
    public ResponseEntity<APIResponse> updateAsset( 
                                    @RequestBody MFUpdateInfo updateInfo ) {
        
        try {
            log.debug( "Updating MF information" ) ;
            
            MutualFundAsset mfAsset = mfAssetRepo.findById( updateInfo.getId() )
                                                 .get() ;
            mfAsset.setIsin( updateInfo.getIsin() ) ;
            mfAsset.setUrl( updateInfo.getUrl() ) ;
            mfAsset.setPurpose( updateInfo.getPurpose() ) ;
            
            mfAssetRepo.save( mfAsset ) ;
            
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
    
    
    private void saveMFAsset( MutualFundAsset postedAsset )
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

    private boolean saveMFTxn( MFTxn postedTxn )
        throws Exception {
        
        MutualFundAsset mf = mfAssetRepo.findByOwnerNameAndScheme( 
                                                    postedTxn.getOwnerName(), 
                                                    postedTxn.getScheme() ) ;
        
        String txnHash = generateHash( postedTxn.getOwnerName(), postedTxn ) ;
        MutualFundTxn existingTxn = mfTxnRepo.findByHash( txnHash ) ;
        
        if( existingTxn == null ) {
            
            MutualFundTxn mfTxn = new MutualFundTxn() ;
            mfTxn.setMfId( mf.getId() ) ;
            mfTxn.setTxnDate( postedTxn.getTxnDate() ) ;
            mfTxn.setTxnType( postedTxn.getTxnType() ) ;
            mfTxn.setTxnChannel( postedTxn.getTxnChannel() ) ;
            mfTxn.setNavPerUnit( postedTxn.getNavPerUnit() ) ;
            mfTxn.setNumUnits( postedTxn.getNumUnits() ) ;
            mfTxn.setAmount( postedTxn.getAmount() ) ;
            mfTxn.setHash( txnHash ) ;
            
            mfTxnRepo.save( mfTxn ) ;
            return true ;
        }
        return false ;
    }
    
    private String generateHash( String ownerName, MFTxn txn ) {
        
        StringBuilder builder = new StringBuilder() ;
        builder.append( ownerName )
               .append( SDF.format( txn.getTxnDate() ) )
               .append( txn.getTxnType() )
               .append( DF.format( txn.getNavPerUnit() ) )
               .append( DF.format( txn.getNumUnits() ) )
               .append( DF.format( txn.getAmount() ) ) ;
        return StringUtil.getHash( builder.toString() ) ;
    }
}
