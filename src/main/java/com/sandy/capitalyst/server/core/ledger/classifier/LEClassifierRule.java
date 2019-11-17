package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public abstract class LEClassifierRule {
    
    public abstract boolean isRuleMatched( LedgerEntry ledgerEntry ) ;
    
    public abstract String getFormattedString( String indent ) ;
}
