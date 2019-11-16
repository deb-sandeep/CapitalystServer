package com.sandy.capitalyst.server.dao.ledger;

import org.springframework.data.repository.CrudRepository ;

public interface AccountLedgerRepo 
    extends CrudRepository<LedgerEntry, Integer> {
    
    public LedgerEntry findByHash( String hash ) ;
}
