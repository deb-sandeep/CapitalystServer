package com.sandy.capitalyst.server.core.ledger.loader;

import java.util.List ;

public class LedgerImportResult {

    private String fileName = null ;
    private int numEntriesFound = 0 ;
    private int numEntriesImported = 0 ;
    private int numDuplicateEntries = 0 ;
    private List<String> errMsgs = null ;

    public LedgerImportResult() {}

    public void setFileName( String val ) {
        this.fileName = val ;
    }
        
    public String getFileName() {
        return this.fileName ;
    }

    public void setNumEntriesFound( int val ) {
        this.numEntriesFound = val ;
    }
        
    public int getNumEntriesFound() {
        return this.numEntriesFound ;
    }

    public void setNumEntriesImported( int val ) {
        this.numEntriesImported = val ;
    }
        
    public int getNumEntriesImported() {
        return this.numEntriesImported ;
    }

    public void setNumDuplicateEntries( int val ) {
        this.numDuplicateEntries = val ;
    }
        
    public int getNumDuplicateEntries() {
        return this.numDuplicateEntries ;
    }

    public void setErrMsgs( List<String> val ) {
        this.errMsgs = val ;
    }
        
    public List<String> getErrMsgs() {
        return this.errMsgs ;
    }
    
    public void incrementImportCount() {
        this.numEntriesImported++ ;
    }
    
    public void incrementDupCount() {
        this.numDuplicateEntries++ ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "StmtUploadResult [\n" ) ; 

        builder.append( "   fileName = " + this.fileName + "\n" ) ;
        builder.append( "   numEntriesFound = " + this.numEntriesFound + "\n" ) ;
        builder.append( "   numEntriesImported = " + this.numEntriesImported + "\n" ) ;
        builder.append( "   numDuplicateEntries = " + this.numDuplicateEntries + "\n" ) ;
        builder.append( "   errMsgs = " + this.errMsgs + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}