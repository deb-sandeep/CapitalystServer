package com.sandy.capitalyst.server.api.budget.vo;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc.CalendarUtil ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryCategory ;
import com.sandy.common.util.StringUtil ;

import lombok.Getter ;

public class BudgetSpread extends BudgetLineItem {

    private Map<String, L1LineItem> l1LineItemMap = new LinkedHashMap<>() ;
    
    @Getter
    private List<L1LineItem> l1LineItems = new ArrayList<>() ;
    
    public BudgetSpread( Date fyStart, Date fyEnd ) {
        super( "Budget for FY " + CalendarUtil.getYear( fyStart ), 
               fyStart, fyEnd ) ;
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

    public void processEntry( LedgerEntry entry ) {
        
        String l1Cat = entry.getL1Cat() ;
        String l2Cat = entry.getL2Cat() ;
        
        if( StringUtil.isEmptyOrNull( l1Cat ) || 
            StringUtil.isEmptyOrNull( l2Cat ) ) {
            return ;
        }
        
        L1LineItem l1LineItem = l1LineItemMap.get( l1Cat ) ;
        if( l1LineItem != null ) {
            l1LineItem.processEntry( entry ) ;
        }
    }
    
    @Override
    public void computeBudgetOverflows() {
        for( L1LineItem lineItem : l1LineItems ) {
            lineItem.computeBudgetOverflows() ;
        }
        super.computeBudgetOverflows() ;
    }
}
