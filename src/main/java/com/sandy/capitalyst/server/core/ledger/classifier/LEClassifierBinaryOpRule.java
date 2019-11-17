package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierBinaryOpRule extends LEClassifierRule {

    private String op = null ;
    private LEClassifierRule leftRule = null ;
    private LEClassifierRule rightRule = null ;
    
    public LEClassifierBinaryOpRule( String op ) {
        this.op = op ;
    }
    
    public void setLeftRule( LEClassifierRule rule ) {
        this.leftRule = rule ;
    }
    
    public void setRightRule( LEClassifierRule rule ) {
        this.rightRule = rule ;
    }

    public boolean isRuleMatched( LedgerEntry ledgerEntry ) {
        
        boolean isLeftRuleMatched = leftRule.isRuleMatched( ledgerEntry ) ;
        
        if( op.equals( "OR" ) ) {
            if( isLeftRuleMatched ) return true ;
            else {
                return rightRule.isRuleMatched( ledgerEntry ) ;
            }
        }
        else {
            if( !isLeftRuleMatched ) return false ;
            else {
                return rightRule.isRuleMatched( ledgerEntry ) ;
            }
        }
    }

    public String getFormattedString( String indent ) {
        return indent + op + "\n" + 
               leftRule.getFormattedString( indent + "    " ) + "\n" + 
               rightRule.getFormattedString( indent + "    " ) ;
    }
}
