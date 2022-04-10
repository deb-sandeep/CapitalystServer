package com.sandy.capitalyst.server.core.ledger.classifier;

import org.springframework.util.StringUtils ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public abstract class LEClassifierRule {
    
    protected String ruleName = null ;
    
    protected LEClassifierRule( String ruleName ) {
        this.ruleName = ruleName ;
    }
    
    protected String formatRuleValue( String value ) {
        value = value.replaceAll( "\\.\\*", "" ) ;
        value = StringUtils.capitalize( value ) ;
        return value ;
    }
    
    /**
     * Returns null if the rule did not match the entry, else 
     * returns a string which will be used to auto populate the 
     * notes of the ledger entry.
     */
    public abstract String getMatchResult( LedgerEntry ledgerEntry ) ;
    
    public abstract String getFormattedString( String indent ) ;
}
