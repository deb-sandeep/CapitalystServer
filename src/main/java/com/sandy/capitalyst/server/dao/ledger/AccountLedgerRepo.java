package com.sandy.capitalyst.server.dao.ledger;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

public interface AccountLedgerRepo 
    extends CrudRepository<LedgerEntry, Integer> {
    
    public LedgerEntry findByHash( String hash ) ;
    
    @Query( value =   
            "SELECT "
          + "    le "
          + "FROM "
          + "    LedgerEntry le "
          + "WHERE "
          + "    le.account.id = :accountId AND "
          + "    le.valueDate >= :startDate AND "
          + "    le.valueDate <= :endDate "
          + "ORDER BY "
          + "    le.valueDate DESC "
    )
    public List<LedgerEntry> findEntriesByDateRange( 
                                    @Param( "accountId" ) Integer accountId,
                                    @Param( "startDate" ) Date startDate,
                                    @Param( "endDate"   ) Date endDate ) ; 
}
