package com.sandy.capitalyst.server.dao.ledger.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.ledger.DebitCreditAssoc ;

public interface DebitCreditAssocRepo 
    extends CrudRepository<DebitCreditAssoc, Integer> {
    
    public List<DebitCreditAssoc> findByCreditTxnId( Integer id ) ;
    
    public List<DebitCreditAssoc> findByDebitTxnId( Integer id ) ;
    
    @Query( 
         "SELECT DISTINCT " 
        + "   dca.debitTxnId "
        + "FROM " 
        + "   DebitCreditAssoc dca" 
    )
    public List<Integer> findDistinctDebitTxnId() ;

    @Query( 
            "SELECT DISTINCT " 
           + "   dca.creditTxnId "
           + "FROM " 
           + "   DebitCreditAssoc dca" 
       )
    public List<Integer> findDistinctCreditTxnId() ;
}
