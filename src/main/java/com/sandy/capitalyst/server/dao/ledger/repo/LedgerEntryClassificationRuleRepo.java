package com.sandy.capitalyst.server.dao.ledger.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntryClassificationRule ;

public interface LedgerEntryClassificationRuleRepo 
    extends CrudRepository<LedgerEntryClassificationRule, Integer> {
    
    public LedgerEntry findByRuleName( String ruleName ) ;
}
