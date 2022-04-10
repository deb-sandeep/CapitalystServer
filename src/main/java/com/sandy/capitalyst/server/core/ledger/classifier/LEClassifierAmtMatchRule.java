package com.sandy.capitalyst.server.core.ledger.classifier;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class LEClassifierAmtMatchRule extends LEClassifierRule {

    public static enum OpType { EQ, LT, GT, BW } ;
    
    private OpType opType = null ;
    private Float amt = null ;
    private Float minAmt = null ;
    private Float maxAmt = null ;
    
    public LEClassifierAmtMatchRule( String ruleName, OpType opType ) {
        super( ruleName ) ;
        this.opType = opType ;
    }
    
    public void setAmt( Float amt ) {
        this.amt = amt ;
    }
    
    public void setMinAmt( Float amt ) {
        this.minAmt = amt ;
    }
    
    public void setMaxAmt( Float amt ) {
        this.maxAmt = amt ;
    }

    public String getMatchResult( LedgerEntry entry ) {
        
        boolean matched = false ;
        
        switch( opType ) {
            case EQ:
                matched = ( amt == entry.getAmount() ) ;
                break ;
            case GT:
                matched = ( amt >= entry.getAmount() ) ;
                break ;
            case LT:
                matched = ( amt <= entry.getAmount() ) ;
                break ;
            case BW:
                matched = ( minAmt >= entry.getAmount() && 
                            maxAmt <= entry.getAmount() ) ;
                break ;
        }
        
        return ( matched ) ? super.ruleName : null ;
    }

    public String getFormattedString( String indent ) {
        StringBuilder builder = new StringBuilder( indent + "Amt " ) ;
        builder.append( opType ).append( " " ) ;
        if( opType == OpType.BW ) {
            builder.append( minAmt + " : " + maxAmt ) ;
        }
        else {
            builder.append( amt ) ;
        }
        return builder.toString() ;
    }
}
