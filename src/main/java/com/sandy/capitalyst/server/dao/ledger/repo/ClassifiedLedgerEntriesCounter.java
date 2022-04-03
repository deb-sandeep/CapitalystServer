package com.sandy.capitalyst.server.dao.ledger.repo;

import lombok.Data ;

public interface ClassifiedLedgerEntriesCounter {

    public byte    getIsCreditEntry() ;
    public String  getL1CatName() ;
    public String  getL2CatName() ;
    public int     getNumEntries() ;
}
