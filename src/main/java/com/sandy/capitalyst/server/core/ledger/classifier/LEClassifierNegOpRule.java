package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierNegOpRule extends LEClassifierRule {

    private LEClassifierRule rule = null ;
    
    public LEClassifierNegOpRule( String ruleName, LEClassifierRule rule ) {
        super( ruleName ) ;
        this.rule = rule ;
    }

    public String getMatchResult( LedgerEntry ledgerEntry ) {
        
        String ruleMatchResult = rule.getMatchResult( ledgerEntry ) ;
        
        // A neg op rule matches as true when the underlying rule is not a match
        if( ruleMatchResult == null ) {
            return "!" + ruleName ;
        }
        return null ;
    }

    public String getFormattedString( String indent ) {
        return indent + "NOT " + "\n" + 
               rule.getFormattedString( indent + "    " ) ;
    }
}
