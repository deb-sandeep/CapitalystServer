package com.sandy.capitalyst.server.api.budget.vo;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;

import lombok.Getter ;

public class BudgetSpread extends BudgetLineItem {

    private Map<String, L1LineItem> l1LineItemMap = new LinkedHashMap<>() ;
    
    @Getter
    private List<L1LineItem> l1LineItems = new ArrayList<>() ;
    
    public BudgetSpread( String name ) {
        super( name ) ;
    }

    public void addCategory( LedgerEntryCategory cat ) {
        
        String l1Name = cat.getL1CatName() ;
        L1LineItem l1LineItem = null ; 
        
        if( l1LineItemMap.containsKey( l1Name ) ) {
            l1LineItem = l1LineItemMap.get( l1Name ) ; 
        }
        else {
            l1LineItem = new L1LineItem( l1Name, this ) ;
            l1LineItemMap.put( l1Name, l1LineItem ) ;
            l1LineItems.add( l1LineItem ) ;
        }
        
        l1LineItem.addCategory( cat ) ;
    }
}
