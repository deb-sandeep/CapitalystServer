package com.sandy.capitalyst.server.dao.ledger;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;

@Entity
@Table( name = "ledger_entry_categories" )
public class LedgerEntryCategory {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id = null ;
    
    private boolean creditClassification = false ;
    private boolean validForCashEntry = false ;
    private boolean selectedForTxnPivot = false ;
    
    @Column( name = "l1_cat_name" )
    private String l1CatName = null ;
    
    @Column( name = "l2_cat_name" )
    private String l2CatName = null ;

    public LedgerEntryCategory() {}

    public void setId( Integer val ) {
        this.id = val ;
    }
        
    public Integer getId() {
        return this.id ;
    }

    public void setCreditClassification( boolean val ) {
        this.creditClassification = val ;
    }
        
    public boolean isCreditClassification() {
        return this.creditClassification ;
    }

    public void setL1CatName( String val ) {
        this.l1CatName = val ;
    }
        
    public String getL1CatName() {
        return this.l1CatName ;
    }

    public void setL2CatName( String val ) {
        this.l2CatName = val ;
    }
        
    public String getL2CatName() {
        return this.l2CatName ;
    }
    
    public boolean isValidForCashEntry() {
        return validForCashEntry ;
    }

    public void setValidForCashEntry( boolean validForCashEntry ) {
        this.validForCashEntry = validForCashEntry ;
    }

    public boolean isSelectedForTxnPivot() {
        return selectedForTxnPivot;
    }

    public void setSelectedForTxnPivot( boolean selectedForTxnPivot ) {
        this.selectedForTxnPivot = selectedForTxnPivot;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerEntryCategory [\n" ) ; 

        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   isCreditClassification = " + this.creditClassification + "\n" ) ;
        builder.append( "   isSelectedForTxnPivot = " + this.selectedForTxnPivot + "\n" ) ;
        builder.append( "   validForCashEntry = " + this.validForCashEntry + "\n" ) ;
        builder.append( "   l1CatName = " + this.l1CatName + "\n" ) ;
        builder.append( "   l2CatName = " + this.l2CatName + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
