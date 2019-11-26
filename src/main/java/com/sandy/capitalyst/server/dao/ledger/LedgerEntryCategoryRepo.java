package com.sandy.capitalyst.server.dao.ledger;

import org.springframework.data.repository.CrudRepository ;

public interface LedgerEntryCategoryRepo 
    extends CrudRepository<LedgerEntryCategory, Integer> {
}
