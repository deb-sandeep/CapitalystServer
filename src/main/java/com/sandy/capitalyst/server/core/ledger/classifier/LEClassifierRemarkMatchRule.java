package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.ArrayList ;
import java.util.List ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierRemarkMatchRule extends LEClassifierRule {

    private List<String>  regexes  = new ArrayList<>() ;
    private List<Pattern> patterns = new ArrayList<>() ;
    
    public LEClassifierRemarkMatchRule( List<String> regexList ) {
        
        for( String regex : regexList ) {
            
            regex = regex.replace( "*", ".*" ) ;
            Pattern pattern = Pattern.compile( regex.toLowerCase() ) ;
            
            regexes.add( regex ) ;
            patterns.add( pattern ) ;
        }
    }

    public boolean isRuleMatched( LedgerEntry ledgerEntry ) {
        
        for( Pattern pattern : patterns ) {
            Matcher m = pattern.matcher( ledgerEntry.getRemarks().toLowerCase() ) ;
            if( m.matches() ) {
                return true ;
            }
        }
        return false ;
    }

    public String getFormattedString( String indent ) {
        StringBuilder sb = new StringBuilder( indent ) ;
        for( String regex : regexes ) {
            sb.append( regex + " | " ) ;
        }
        return sb.toString() ;
    }
}
