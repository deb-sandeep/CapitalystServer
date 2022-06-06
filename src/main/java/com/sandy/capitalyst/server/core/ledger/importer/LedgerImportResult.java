package com.sandy.capitalyst.server.core.ledger.importer;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Data ;

@Data
public class LedgerImportResult {

    private String fileName = null ;
    private int numEntriesFound = 0 ;
    private int numEntriesImported = 0 ;
    private int numDuplicateEntries = 0 ;
    private List<String> errMsgs = new ArrayList<>() ;

    public void incrementImportCount() {
        this.numEntriesImported++ ;
    }
    
    public void incrementDupCount() {
        this.numDuplicateEntries++ ;
    }
    
    public boolean hasErrors() {
        return !errMsgs.isEmpty() ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerImportResult [\n" ) ; 

        builder.append( "   fileName = " + fileName + "\n" ) ;
        builder.append( "   numEntriesFound = " + numEntriesFound + "\n" ) ;
        builder.append( "   numEntriesImported = " + numEntriesImported + "\n" ) ;
        builder.append( "   numDuplicateEntries = " + numDuplicateEntries + "\n" ) ;
        builder.append( "   errMsgs = " + errMsgs + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}