package com.sandy.capitalyst.server.api.ledger.helpers;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryCategoryRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerEntryClassificationRuleRepo ;
import com.sandy.capitalyst.server.dao.ledger.repo.LedgerRepo ;

public class ChangedCategorySaveHelper {
    
    private static final Logger log = Logger.getLogger( ChangedCategorySaveHelper.class ) ;

    private List<LedgerEntryCategory> categories = null ;
    
    private LedgerRepo                        lRepo    = null ;
    private LedgerEntryCategoryRepo           lecRepo  = null ;
    private LedgerEntryClassificationRuleRepo lecrRepo = null ;
    
    public ChangedCategorySaveHelper( 
                                List<LedgerEntryCategory> categories,
                                LedgerRepo lRepo,
                                LedgerEntryCategoryRepo lecRepo,
                                LedgerEntryClassificationRuleRepo lecrRepo ) {
        
        this.categories = categories ;
        this.lRepo = lRepo ;
        this.lecRepo = lecRepo ;
        this.lecrRepo = lecrRepo ;
    }
    
    public void save() throws Exception {
        for( LedgerEntryCategory cat : categories ) {
            saveCategory( cat ) ;
        }
    }
    
    private void saveCategory( LedgerEntryCategory newCat )
        throws Exception {
        
        log.debug( "Saving changes to category " + newCat.getId() ) ;
        LedgerEntryCategory oldCat = lecRepo.findById( newCat.getId() ).get() ;
        
        String newL1CatName = newCat.getL1CatName() ;
        String newL2CatName = newCat.getL2CatName() ;
        String oldL1CatName = oldCat.getL1CatName() ;
        String oldL2CatName = oldCat.getL2CatName() ;
        
        boolean isCreditClass = oldCat.isCreditClassification() ;
        
        log.debug( "Old category = " + oldCat ) ;
        log.debug( "New category = " + newCat ) ;
        
        if( hasCatNamesChanged( newCat, oldCat ) ) {
            // If either of the category names have changed, we have to 
            // update the ledger and the ledger classification rules
            
            log.debug( "Category name change detected." ) ;
            
            updateLedgerEntries( newL1CatName, newL2CatName, 
                                 oldL1CatName, oldL2CatName, 
                                 isCreditClass ) ;
            
            updateClassificationRules( oldCat, newL1CatName, newL2CatName,
                                               oldL1CatName, oldL2CatName ) ;
        }
        
        log.debug( "Updating attributes of existing category" ) ;
        oldCat.copyAttributes( newCat ) ;
        lecRepo.save( oldCat ) ;
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
    
    private boolean hasCatNamesChanged( LedgerEntryCategory newCat, 
                                        LedgerEntryCategory oldCat ) {
        
        if( !newCat.getL1CatName().equals( oldCat.getL1CatName() ) ) {
            return true ;
        }
        
        if( !newCat.getL2CatName().equals( oldCat.getL2CatName() ) ) {
            return true ;
        }
        
        return false ;
    }
}
