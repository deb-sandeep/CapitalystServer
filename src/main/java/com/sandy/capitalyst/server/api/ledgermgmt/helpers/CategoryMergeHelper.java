package com.sandy.capitalyst.server.api.ledgermgmt.helpers;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public class CategoryMergeHelper {
    
    private static final Logger log = Logger.getLogger( CategoryMergeHelper.class ) ;

    private MergeLedgeEntryCategoriesInput input = null ;
    
    private LedgerRepo                        lRepo    = null ;
    private LedgerEntryCategoryRepo           lecRepo  = null ;
    private LedgerEntryClassificationRuleRepo lecrRepo = null ;
    
    public CategoryMergeHelper( MergeLedgeEntryCategoriesInput input,
                                LedgerRepo lRepo,
                                LedgerEntryCategoryRepo lecRepo,
                                LedgerEntryClassificationRuleRepo lecrRepo ) {
        
        this.input = input ;
        this.lRepo = lRepo ;
        this.lecRepo = lecRepo ;
        this.lecrRepo = lecrRepo ;
    }
    
    public void merge() throws Exception {
        
        // The category which will be merged into the new category.
        LedgerEntryCategory oldCat = null ;
        LedgerEntryCategory newCat = null ;
        boolean isCreditClass = false ;
        
        oldCat = lecRepo.findById( input.getOldCatId() ).get() ;
        isCreditClass = oldCat.isCreditClassification() ;
        
        newCat = lecRepo.findCategory( isCreditClass, 
                                       input.getNewL1CatName(), 
                                       input.getNewL2CatName() ) ; 
        
        if( newCat == null ) {
            // Create a new category in case it doesn't exist
            newCat = new LedgerEntryCategory() ;
            newCat.setCreditClassification( isCreditClass ) ;
            newCat.setL1CatName( input.getNewL1CatName() ) ;
            newCat.setL2CatName( input.getNewL2CatName() ) ;
            newCat.setSelectedForTxnPivot( oldCat.isSelectedForTxnPivot() ) ;
            newCat.setValidForCashEntry( oldCat.isValidForCashEntry() ) ;
            
            lecRepo.save( newCat ) ;
        }
        
        // If the new category already exists, just update the ledger
        // entries
        updateLedgerEntries( newCat.getL1CatName(), newCat.getL2CatName(),
                             oldCat.getL1CatName(), oldCat.getL2CatName(),
                             isCreditClass ) ;
        
        updateClassificationRules( oldCat,
                            newCat.getL1CatName(), newCat.getL2CatName(),
                            oldCat.getL1CatName(), oldCat.getL2CatName() ) ;
        
        // Delete the old category
        lecRepo.delete( oldCat ) ;
    }

    private void updateLedgerEntries( String newL1CatName, String newL2CatName,
                                      String oldL1CatName, String oldL2CatName, 
                                      boolean isCreditClass ) {
        
        log.debug( "Updating classification for ledger entries." ) ;
        if( isCreditClass ) {
            lRepo.updateCreditClassificationCategory( 
                                            newL1CatName, newL2CatName, 
                                            oldL1CatName, oldL2CatName ) ;
        }
        else {
            lRepo.updateDebitClassificationCategory( 
                                            newL1CatName, newL2CatName, 
                                            oldL1CatName, oldL2CatName ) ;
        }
    }
    

    private void updateClassificationRules( 
                                LedgerEntryCategory oldCat,
                                String newL1CatName, String newL2CatName, 
                                String oldL1CatName, String oldL2CatName ) {
        
        log.debug( "Updating classification for leger classification rules." ) ;
        
        // Update all the old rules to classify for the new category
        lecrRepo.updateClassificationCategory( 
                                        newL1CatName, newL2CatName, 
                                        oldL1CatName, oldL2CatName ) ;
    }    
}
