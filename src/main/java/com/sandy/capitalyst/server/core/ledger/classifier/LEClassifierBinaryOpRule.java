package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierBinaryOpRule extends LEClassifierRule {

    private String op = null ;
    private LEClassifierRule leftRule = null ;
    private LEClassifierRule rightRule = null ;
    
    public LEClassifierBinaryOpRule( String ruleName, String op ) {
        super( ruleName ) ;
        this.op = op ;
    }
    
    public void setLeftRule( LEClassifierRule rule ) {
        this.leftRule = rule ;
    }
    
    public void setRightRule( LEClassifierRule rule ) {
        this.rightRule = rule ;
    }

    public String getMatchResult( LedgerEntry ledgerEntry ) {
        
        String leftRuleMatchResult = leftRule.getMatchResult( ledgerEntry ) ;
        
        if( op.equals( "OR" ) ) {
            
            if( leftRuleMatchResult != null ) {
                return leftRuleMatchResult ;
            }
            else {
                return rightRule.getMatchResult( ledgerEntry ) ;
            }
        }
        else {
            if( leftRuleMatchResult == null ) {
                return null ;
            }
            else {
                return rightRule.getMatchResult( ledgerEntry ) ;
            }
        }
    }

    public String getFormattedString( String indent ) {
        return indent + op + "\n" + 
               leftRule.getFormattedString( indent + "    " ) + "\n" + 
               rightRule.getFormattedString( indent + "    " ) ;
    }
}
