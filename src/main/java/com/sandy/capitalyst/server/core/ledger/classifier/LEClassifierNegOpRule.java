package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierNegOpRule extends LEClassifierRule {

    private LEClassifierRule rule = null ;
    
    public LEClassifierNegOpRule( LEClassifierRule rule ) {
        this.rule = rule ;
    }

    public boolean isRuleMatched( LedgerEntry ledgerEntry ) {
        return !rule.isRuleMatched( ledgerEntry ) ;
    }

    public String getFormattedString( String indent ) {
        return indent + "NOT " + "\n" + 
               rule.getFormattedString( indent + "    " ) ;
    }
}
