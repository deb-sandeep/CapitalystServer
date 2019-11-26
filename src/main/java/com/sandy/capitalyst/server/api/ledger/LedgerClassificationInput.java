package com.sandy.capitalyst.server.api.ledger;

import java.util.Arrays ;

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

    public LedgerClassificationInput() {}

    public void setEntryIdList( Integer[] val ) {
        this.entryIdList = val ;
    }
        
    public Integer[] getEntryIdList() {
        return this.entryIdList ;
    }

    public void setL1Cat( String val ) {
        this.l1Cat = val ;
    }
        
    public String getL1Cat() {
        return this.l1Cat ;
    }

    public void setL2Cat( String val ) {
        this.l2Cat = val ;
    }
        
    public String getL2Cat() {
        return this.l2Cat ;
    }

    public void setNewClassifier( boolean val ) {
        this.newClassifier = val ;
    }
        
    public boolean isNewClassifier() {
        return this.newClassifier ;
    }

    public void setRule( String val ) {
        this.rule = val ;
    }
        
    public String getRule() {
        return this.rule ;
    }

    public void setSaveRule( boolean val ) {
        this.saveRule = val ;
    }
        
    public boolean isSaveRule() {
        return this.saveRule ;
    }
    
    public boolean isCreditClassifier() {
        return creditClassifier ;
    }

    public void setCreditClassifier( boolean creditClassifier ) {
        this.creditClassifier = creditClassifier ;
    }

    public String getRuleName() {
        return ruleName ;
    }

    public void setRuleName( String ruleName ) {
        this.ruleName = ruleName ;
    }

    public String getNotes() {
        return notes ;
    }

    public void setNotes( String notes ) {
        this.notes = notes ;
    }

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

