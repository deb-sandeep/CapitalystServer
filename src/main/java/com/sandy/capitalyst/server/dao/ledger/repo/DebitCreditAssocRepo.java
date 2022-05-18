package com.sandy.capitalyst.server.dao.ledger.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;

public interface DebitCreditAssocRepo 
    extends CrudRepository<DebitCreditAssoc, Integer> {
}
