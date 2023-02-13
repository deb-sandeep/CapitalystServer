package com.sandy.capitalyst.server.dao.ledger;

import javax.persistence.CascadeType ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.JoinColumn ;
import javax.persistence.OneToOne ;
import javax.persistence.Table ;
import javax.persistence.TableGenerator ;
import javax.persistence.Transient ;

import lombok.Data ;

@Data
@Entity
@Table( name = "ledger_category_budget" )
public class LedgerCategoryBudget implements Comparable<LedgerCategoryBudget> {

    @Id
    @TableGenerator(
            name            = "lcbPkGen", 
            table           = "id_gen", 
            pkColumnName    = "gen_key", 
            valueColumnName = "gen_value", 
            pkColumnValue   = "ledger_category_budget_id",
            initialValue    = 1,
            allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="lcbPkGen" )
    private Integer id = null ;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private LedgerEntryCategory category = null ;
    
    private int fy = 0 ;
    private String budgetRule = null ;
    private int yearlyCap = 0 ;

    public LedgerCategoryBudget() {}
    
    @Transient
    public String getL1CatName() {
        return this.category.getL1CatName() ;
    }

    @Transient
    public String getL2CatName() {
        return this.category.getL2CatName() ;
    }

    @Override
    public int compareTo( LedgerCategoryBudget o ) {
        if( !this.getL1CatName().equals( o.getL1CatName() ) ) {
            return this.getL1CatName().compareTo( o.getL1CatName() ) ;
        }
        return this.getL2CatName().compareTo( o.getL2CatName() ) ;
    }
    
    public String toString() {
        
        StringBuilder builder = new StringBuilder( "LedgerEntryCategory [\n" ) ; 

        builder.append( "   id = " + this.id + "\n" ) ;
        builder.append( "   l1Category = " + this.category.getL1CatName() + "\n" ) ;
        builder.append( "   l2Category = " + this.category.getL2CatName() + "\n" ) ;
        builder.append( "   yearlyCap  = " + this.yearlyCap + "\n" ) ;
        builder.append( "   budgetRule = " + this.budgetRule + "\n" ) ;
        
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}
