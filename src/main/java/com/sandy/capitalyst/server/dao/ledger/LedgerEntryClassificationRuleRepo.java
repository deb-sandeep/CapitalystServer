package com.sandy.capitalyst.server.dao.ledger;

import org.springframework.data.repository.CrudRepository ;

public interface LedgerEntryClassificationRuleRepo 
    extends CrudRepository<LedgerEntryClassificationRule, Integer> {
    
    public LedgerEntry findByRuleName( String ruleName ) ;
}
