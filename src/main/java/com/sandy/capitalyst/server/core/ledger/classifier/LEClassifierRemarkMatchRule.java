package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.ArrayList ;
import java.util.List ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierRemarkMatchRule extends LEClassifierRule {

    private List<String>  regexes  = new ArrayList<>() ;
    private List<Pattern> patterns = new ArrayList<>() ;
    
    public LEClassifierRemarkMatchRule( String ruleName, List<String> regexList ) {
        
        super( ruleName ) ;
        
        for( String regex : regexList ) {
            
            regex = regex.replace( "*", ".*" ) ;
            Pattern pattern = Pattern.compile( regex.toLowerCase() ) ;
            
            regexes.add( regex ) ;
            patterns.add( pattern ) ;
        }
    }

    public String getMatchResult( LedgerEntry ledgerEntry ) {
        
        for( int i=0; i<patterns.size(); i++ ) {
            
            Pattern pattern = patterns.get( i ) ;
            String  regex   = regexes.get( i ) ;
            
            Matcher m = pattern.matcher( ledgerEntry.getRemarks().toLowerCase() ) ;
            if( m.matches() ) {
                return formatRuleValue( ruleName + " - " + regex ) ;
            }
        }
        return null ;
    }

    public String getFormattedString( String indent ) {
        StringBuilder sb = new StringBuilder( indent ) ;
        for( String regex : regexes ) {
            sb.append( regex + " | " ) ;
        }
        return sb.toString() ;
    }
}
