package com.sandy.capitalyst.server.api.equity.helper.bhavcopy;

import java.util.Date ;

import lombok.Data ;

@Data
public class BhavcopyImportResult {

    private String fileName = null ;
    private Date bhavcopyDate = null ;
    private int numRecordsFound = 0 ;
    private int numRecordsImported = 0 ;
    
    public void incNumRecordsImported() {
        this.numRecordsImported++ ;
    }
}
