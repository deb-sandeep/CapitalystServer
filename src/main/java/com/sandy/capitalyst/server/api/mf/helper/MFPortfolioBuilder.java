package com.sandy.capitalyst.server.api.mf.helper;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAsset ;
import com.sandy.capitalyst.server.dao.mf.MutualFundAssetRepo ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxn ;
import com.sandy.capitalyst.server.dao.mf.MutualFundTxnRepo ;

class MFLot {
    
    float numUnitsLeft = 0 ;
    Date  txnDate = null ;
    
    public MFLot( float units, Date date ) {
        this.numUnitsLeft = units ;
        this.txnDate = date ;
    }
    
    // Returns the number of units that COULD NOT be redeemed
    public float redeemUnits( float unitsToBeRedeemed ) {
        float redeemedUnits = 0 ;
        if( numUnitsLeft > 0 ) {
            if( unitsToBeRedeemed <= numUnitsLeft ) {
                redeemedUnits = unitsToBeRedeemed ;
            }
            else {
                redeemedUnits = numUnitsLeft ;
            }
        }
        numUnitsLeft -= redeemedUnits ;
        
        float unRedeemedUnits = unitsToBeRedeemed - redeemedUnits ;
        return unRedeemedUnits ;
    }
    
    public int getTenureInDays() {
        long numMillis = new Date().getTime() - txnDate.getTime() ;
        return (int)( numMillis / (1000*60*60*24) ) ;
    }
    
    public boolean qualifiesForLTCG() {
        Date oneYearPastDate = DateUtils.addYears( new Date(), -1 ) ;
        return txnDate.before( oneYearPastDate ) ;
    }
}

public class MFPortfolioBuilder {
    
    static final Logger log = Logger.getLogger( MFPortfolioBuilder.class ) ;

    private MutualFundAssetRepo mfAssetRepo = null ;
    private MutualFundTxnRepo mfTxnRepo = null ;
    
    public MFPortfolioBuilder() {
        mfAssetRepo = CapitalystServer.getBean( MutualFundAssetRepo.class ) ;
        mfTxnRepo = CapitalystServer.getBean( MutualFundTxnRepo.class ) ;
    }
    
    public List<MFHolding> getMFPortfolio() {
        
        List<MutualFundAsset> assets = null ;
        List<MFHolding> holdings = new ArrayList<>() ;
        
        assets = mfAssetRepo.findByUnitsHeldGreaterThanOrderByOwnerNameAsc( 0 ) ;
        for( MutualFundAsset asset : assets ) {
            holdings.add( buildMFHolding( asset ) ) ;
        }
        return holdings ;
    }
    
    private MFHolding buildMFHolding( MutualFundAsset asset ) {
        
        MFHolding holding = new MFHolding() ;
        holding.setAssetId( asset.getId() ) ;
        holding.setOwnerName( asset.getOwnerName() ) ;
        holding.setIsin( asset.getIsin() ) ;
        holding.setUrl( asset.getUrl() ) ;
        holding.setScheme( asset.getScheme() ) ;
        holding.setCategory( asset.getCategory() ) ;
        holding.setSubCategory( asset.getSubCategory() ) ;
        holding.setPurpose( asset.getPurpose() ) ;
        holding.setUnitsHeld( asset.getUnitsHeld() ) ;
        holding.setCurrentNav( asset.getLastRecordedNav() ) ;
        holding.setAvgCostPrice( asset.getAvgCostPrice() ) ;
        holding.setValueAtCost( asset.getValueAtCost() ) ;
        holding.setValueAtNav( asset.getValueAtNav() ) ;
        
        computeDerivativeValues( asset.getId(), holding ) ;
        
        return holding ;
    }
    
    private void computeDerivativeValues( Integer mfId, MFHolding holding ) {
        
        List<MutualFundTxn> txns = mfTxnRepo.findByMfIdOrderByTxnDateAsc( mfId ) ;
        List<MFLot> allLots = new ArrayList<>() ;
        
        for( MutualFundTxn txn : txns ) {
            String txnType = txn.getTxnType().toLowerCase() ;
            if( txnType.contains( "purchase" ) ) {
                MFLot lot = new MFLot( txn.getNumUnits(), txn.getTxnDate() ) ;
                allLots.add( lot ) ;
            }
            else if( txnType.contains( "redemption" ) ) {
                float unRedeemedUnits = txn.getNumUnits() ;
                Iterator<MFLot> allLotsIter = allLots.iterator() ;
                
                while( unRedeemedUnits > 0 && allLotsIter.hasNext() ) {
                    MFLot lot = allLotsIter.next() ;
                    unRedeemedUnits = lot.redeemUnits( unRedeemedUnits ) ;
                }
            }
            else {
                throw new RuntimeException( "Unrecognized txn type for MF Txn" ) ;
            }
        }

        calculateTaxGroupings( holding, allLots ) ;
    }
    
    private void calculateTaxGroupings( MFHolding holding, 
                                        List<MFLot> allLots ) {
        
        float numUnits = 0 ;
        float numUnitsQualifiedForLTCG = 0 ;
        float numUnitsQualifiedForSTCG = 0 ;
        float weightedDays = 0 ;
        
        for( MFLot lot : allLots ) {
            if( lot.numUnitsLeft > 0 ) {
                weightedDays += ( lot.numUnitsLeft * lot.getTenureInDays() ) ;
                if( lot.qualifiesForLTCG() ) {
                    numUnitsQualifiedForLTCG += lot.numUnitsLeft ;
                }
                else {
                    numUnitsQualifiedForSTCG += lot.numUnitsLeft ;
                }
            }
        }
        
        numUnits = numUnitsQualifiedForLTCG + numUnitsQualifiedForSTCG ;
        
        holding.setAverageHoldingDays( (int)(weightedDays / numUnits) ) ;
        holding.setNumUnitsQualifiedForLTCG( numUnitsQualifiedForLTCG ) ;
        
        float valueAtCostForLTCGQty = holding.getAvgCostPrice() * numUnitsQualifiedForLTCG ;
        float valueAtNavForLTCGQty = holding.getCurrentNav() * numUnitsQualifiedForLTCG ;
        float profitLossForLTCGQty = valueAtNavForLTCGQty - valueAtCostForLTCGQty ;
        float ltcgTaxAmt = 0 ;
        
        if( profitLossForLTCGQty > 0 ) {
            ltcgTaxAmt = (float)(0.1 * profitLossForLTCGQty) ;
        }
        
        holding.setLtcgValueAtNavAfterTax( valueAtNavForLTCGQty - ltcgTaxAmt ) ;
        holding.setLtcgProfitLossAmtAfterTax( profitLossForLTCGQty - ltcgTaxAmt ) ;
        holding.setLtcgProfitLossPctAfterTax( (holding.getLtcgProfitLossAmtAfterTax()*100)/valueAtCostForLTCGQty ) ;
        
        float valueAtCostForSTCGQty = holding.getAvgCostPrice() * numUnitsQualifiedForSTCG ;
        float valueAtNavForSTCGQty = holding.getCurrentNav() * numUnitsQualifiedForSTCG ;
        float profitLossForSTCGQty = valueAtNavForSTCGQty - valueAtCostForSTCGQty ;
        float stcgTaxAmt = 0 ;
        
        if( profitLossForSTCGQty > 0 ) {
            stcgTaxAmt = (float)(0.3 * profitLossForSTCGQty) ;
        }
        
        float totalTax = ltcgTaxAmt + stcgTaxAmt ;
        holding.setValueAtNavAfterTax( numUnits*holding.getCurrentNav() - totalTax ) ;
        holding.setProfitLossAmtAfterTax( holding.getValueAtNavAfterTax() - holding.getValueAtCost() ) ;
        holding.setProfitLossAmtPctAfterTax( ( holding.getProfitLossAmtAfterTax() * 100 )/holding.getValueAtCost() ) ;
    }
    
}
