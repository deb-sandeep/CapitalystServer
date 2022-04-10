package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierL2CatMatchRule extends LEClassifierRule {

    private String regex = null ;
    private Pattern pattern = null ;
    
    public LEClassifierL2CatMatchRule( String ruleName, String regex ) {
        super( ruleName ) ;
        this.regex = regex.replace( "*", ".*" ) ;
        this.pattern = Pattern.compile( this.regex.toLowerCase() ) ;
    }

    public String getMatchResult( LedgerEntry ledgerEntry ) {
        
        String cat = ledgerEntry.getL2Cat() ;
        cat = ( cat == null ) ? "" : cat.toLowerCase() ;
        Matcher m = pattern.matcher( cat ) ;
        
        if( m.matches() ) {
            return formatRuleValue( ruleName + " - " + regex ) ;
        }
        return null ;
    }

    public String getFormattedString( String indent ) {
        return indent + "L2Cat ~ " + regex ;
    }
}
