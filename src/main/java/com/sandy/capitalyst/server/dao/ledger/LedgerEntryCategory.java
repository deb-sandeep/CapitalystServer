package com.sandy.capitalyst.server.dao.ledger;

import jakarta.persistence.Column ;
import jakarta.persistence.Entity ;
import jakarta.persistence.GeneratedValue ;
import jakarta.persistence.GenerationType ;
import jakarta.persistence.Id ;
import jakarta.persistence.Table ;

import lombok.Data ;

@Data
@Entity
@Table( name = "ledger_entry_categories" )
public class LedgerEntryCategory implements Comparable<LedgerEntryCategory> {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id = null ;
    
    private boolean creditClassification = false ;
    private boolean validForCashEntry = false ;
    private boolean selectedForTxnPivot = false ;
    
    @Column( name = "l1_cat_name" )
    private String l1CatName = null ;
    
    @Column( name = "l2_cat_name" )
    private String l2CatName = null ;
    
    public LedgerEntryCategory() {}

    public String toString() {
        
        StringBuilder builder = new StringBuilder( "LedgerEntryCategory [\n" ) ; 

        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   isCreditClassification = " + this.creditClassification + "\n" ) ;
        builder.append( "   isSelectedForTxnPivot  = " + this.selectedForTxnPivot + "\n" ) ;
        builder.append( "   validForCashEntry      = " + this.validForCashEntry + "\n" ) ;
        builder.append( "   l1CatName              = " + this.l1CatName + "\n" ) ;
        builder.append( "   l2CatName              = " + this.l2CatName + "\n" ) ;
        
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
    
    public void copyAttributes( LedgerEntryCategory refCat ) {
        
        this.creditClassification = refCat.creditClassification ;
        this.validForCashEntry    = refCat.validForCashEntry ;
        this.selectedForTxnPivot  = refCat.selectedForTxnPivot ;
        this.l1CatName            = refCat.l1CatName ;
        this.l2CatName            = refCat.l2CatName ;
    }

    @Override
    public int compareTo( LedgerEntryCategory o ) {
        if( this.creditClassification != o.creditClassification ) {
            if( this.creditClassification ) {
                return -1 ;
            }
            return 1 ;
        }
        
        if( !this.l1CatName.equals( o.l1CatName ) ) {
            return this.l1CatName.compareTo( o.l1CatName ) ;
        }
        
        return this.l2CatName.compareTo( o.l2CatName ) ;
    }
}
