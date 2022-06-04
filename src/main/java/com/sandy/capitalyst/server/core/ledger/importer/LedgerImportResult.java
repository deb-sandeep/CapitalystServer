package com.sandy.capitalyst.server.core.ledger.importer;

import java.util.List ;

import lombok.Data ;

@Data
public class LedgerImportResult {

    private String fileName = null ;
    private int numEntriesFound = 0 ;
    private int numEntriesImported = 0 ;
    private int numDuplicateEntries = 0 ;
    private List<String> errMsgs = null ;

    public void incrementImportCount() {
        this.numEntriesImported++ ;
    }
    
    public void incrementDupCount() {
        this.numDuplicateEntries++ ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "StmtUploadResult [\n" ) ; 

        builder.append( "   fileName = " + fileName + "\n" ) ;
        builder.append( "   numEntriesFound = " + numEntriesFound + "\n" ) ;
        builder.append( "   numEntriesImported = " + numEntriesImported + "\n" ) ;
        builder.append( "   numDuplicateEntries = " + numDuplicateEntries + "\n" ) ;
        builder.append( "   errMsgs = " + errMsgs + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}