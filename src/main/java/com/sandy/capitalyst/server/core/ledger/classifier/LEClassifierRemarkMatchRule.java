package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierRemarkMatchRule extends LEClassifierRule {

    private String regex = null ;
    private Pattern pattern = null ;
    
    public LEClassifierRemarkMatchRule( String regex ) {
        this.regex = regex.replace( "*", ".*" ) ;
        this.pattern = Pattern.compile( this.regex.toLowerCase() ) ;
    }

    public boolean isRuleMatched( LedgerEntry ledgerEntry ) {
        Matcher m = pattern.matcher( ledgerEntry.getRemarks().toLowerCase() ) ;
        return m.matches() ;
    }

    public String getFormattedString( String indent ) {
        return indent + "Remark ~ " + regex ;
    }
}
