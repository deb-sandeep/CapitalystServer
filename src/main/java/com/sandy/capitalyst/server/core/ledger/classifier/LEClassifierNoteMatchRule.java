package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierNoteMatchRule extends LEClassifierRule {

    private String regex = null ;
    private Pattern pattern = null ;
    
    public LEClassifierNoteMatchRule( String regex ) {
        this.regex = regex.replace( "*", ".*" ) ;
        this.pattern = Pattern.compile( this.regex.toLowerCase() ) ;
    }

    public boolean isRuleMatched( LedgerEntry ledgerEntry ) {
        String cat = ledgerEntry.getNotes() ;
        cat = ( cat == null ) ? "" : cat.toLowerCase() ;
        Matcher m = pattern.matcher( cat ) ;
        return m.matches() ;
    }

    public String getFormattedString( String indent ) {
        return indent + "Note ~ " + regex ;
    }
}
