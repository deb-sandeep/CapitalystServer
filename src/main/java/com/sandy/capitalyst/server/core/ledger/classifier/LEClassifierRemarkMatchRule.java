package com.sandy.capitalyst.server.core.ledger.classifier;

import java.util.ArrayList ;
import java.util.List ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.common.util.StringUtil ;

import lombok.Data ;

public class LEClassifierRemarkMatchRule extends LEClassifierRule {
    
    @Data
    public static class MatchValueWithAlias {
        private String regex = null ;
        private String alias = null ;
    }

    private List<MatchValueWithAlias> matchValues = null ;
    private List<Pattern> patterns = new ArrayList<>() ;
    
    public LEClassifierRemarkMatchRule( String ruleName, 
                                        List<MatchValueWithAlias> regexList ) {
        
        super( ruleName ) ;
        
        this.matchValues = regexList ;
        
        for( MatchValueWithAlias mva : regexList ) {
            
            mva.regex = mva.regex.replace( "*", ".*" ) ;
            
            Pattern pattern = Pattern.compile( mva.regex.toLowerCase() ) ;
            patterns.add( pattern ) ;
        }
    }

    public String getMatchResult( LedgerEntry ledgerEntry ) {
        
        for( int i=0; i<patterns.size(); i++ ) {
            
            Pattern             pattern = patterns.get( i ) ;
            MatchValueWithAlias mva     = matchValues.get( i ) ;
            
            Matcher m = pattern.matcher( ledgerEntry.getRemarks().toLowerCase() ) ;
            
            if( m.matches() ) {
                String matchResult = ruleName ;
                if( StringUtil.isNotEmptyOrNull( mva.alias ) ) {
                    matchResult = mva.alias ;
                }
                return formatRuleValue( matchResult ) ;
            }
        }
        return null ;
    }

    public String getFormattedString( String indent ) {
        
        StringBuilder sb = new StringBuilder( indent ) ;
        for( MatchValueWithAlias mva : matchValues ) {
            sb.append( mva.regex ) ;
            if( StringUtil.isNotEmptyOrNull( mva.alias ) ) {
                sb.append( "@" + mva.alias ) ;
            }
            sb.append( " | " ) ;
        }
        return sb.toString() ;
    }
}
