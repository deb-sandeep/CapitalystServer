package com.sandy.capitalyst.server.api.account;

import java.util.List ;

public class StmtUploadResult {

    private String fileName = null ;
    private int numEntriesFound = 0 ;
    private int numEntriesImported = 0 ;
    private int numDuplicateEntries = 0 ;
    private List<String> errMsgs = null ;

    public StmtUploadResult() {}

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