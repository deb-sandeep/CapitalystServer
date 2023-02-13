package com.sandy.capitalyst.server.api.budget.vo;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.capitalyst.server.dao.ledger.LedgerCategoryBudget ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

import lombok.Getter ;
import lombok.Setter ;

public class L1LineItem extends BudgetLineItem {
    
    BudgetSpread spread = null ;
    
    private Map<String, L2LineItem> l2LineItemMap = new LinkedHashMap<>() ;
    
    @Getter @Setter
    private List<L2LineItem> l2LineItems = new ArrayList<>() ;
    
    @Getter
    private boolean hasBudgedExceedCell = false ;
    
    public L1LineItem( String name, BudgetSpread spread ) {
        super( name, spread.getStartOfYear(), spread.getEndOfYear() ) ;
        this.spread = spread ;
    }

    public void addCategoryBudget( LedgerCategoryBudget catBudget ) {
        
        String l2Name = catBudget.getL2CatName() ;
        L2LineItem l2LineItem = null ; 
        
        if( !l2LineItemMap.containsKey( l2Name ) ) {
            l2LineItem = new L2LineItem( this, catBudget ) ;
            l2LineItemMap.put( l2Name, l2LineItem ) ;
            l2LineItems.add( l2LineItem ) ;
        }
    }

    public void addMonthlyCaps( int[] monthlyCaps ) {
        super.addMonthlyCaps( monthlyCaps ) ;
        spread.addMonthlyCaps( monthlyCaps ) ;
    }

    public void processEntry( LedgerEntry entry ) {
        
        L2LineItem l2LineItem = l2LineItemMap.get( entry.getL2Cat() ) ;
        if( l2LineItem != null ) {
            l2LineItem.processEntry( entry ) ;
            if( !hasBudgedExceedCell ) {
                hasBudgedExceedCell = l2LineItem.hasBudgedExceedCell() ;
            }
        }
    }

    @Override
    public void computeBudgetOverflows() {
        for( L2LineItem lineItem : l2LineItems ) {
            lineItem.computeBudgetOverflows() ;
        }
        super.computeBudgetOverflows() ;
    }
}
