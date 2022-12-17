package com.sandy.capitalyst.server.dao;

import java.util.List ;

public interface IDCompressor {

    public void changeID( Integer oldId, Integer newId ) ;
    
    public List<? extends Object> getBatchOfRecords( Integer offset, Integer batchSize ) ;
}
