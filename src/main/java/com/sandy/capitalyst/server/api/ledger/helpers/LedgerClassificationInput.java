package com.sandy.capitalyst.server.api.ledger.helpers;

import java.util.Arrays ;

import lombok.Data ;

@Data
public class LedgerClassificationInput {

    private Integer[] entryIdList = null ;
    private String l1Cat = null ;
    private String l2Cat = null ;
    private boolean newClassifier = false ;
    private boolean creditClassifier = false ;
    private String rule = null ;
    private boolean saveRule = false ;
    private String ruleName = null ;
    private String notes = null ;

    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerClassificationInput [\n" ) ; 

        builder.append( "   entryIdList = " + Arrays.toString( this.entryIdList ) + "\n" ) ;
        builder.append( "   l1Cat = " + this.l1Cat + "\n" ) ;
        builder.append( "   l2Cat = " + this.l2Cat + "\n" ) ;
        builder.append( "   newClassifier = " + this.newClassifier + "\n" ) ;
        builder.append( "   ruleName = " + this.ruleName + "\n" ) ;
        builder.append( "   rule = " + this.rule + "\n" ) ;
        builder.append( "   saveRule = " + this.saveRule + "\n" ) ;
        builder.append( "   creditClassifier = " + this.creditClassifier + "\n" ) ;
        builder.append( "   notes = " + this.notes + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}

