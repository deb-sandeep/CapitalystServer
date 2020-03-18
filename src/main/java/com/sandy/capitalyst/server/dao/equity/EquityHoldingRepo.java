package com.sandy.capitalyst.server.dao.equity;

import java.util.List ;

import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

public interface EquityHoldingRepo 
    extends CrudRepository<EquityHolding, Integer> {
    
    EquityHolding findByOwnerNameAndSymbolIcici( String ownerName, 
                                                 String symbolIcici ) ;
    
    @Query( value =   
            "SELECT "
          + "    eh "
          + "FROM "
          + "    EquityHolding eh "
          + "WHERE "
          + "    eh.quantity >0 "
    )
    List<EquityHolding> findNonZeroHoldings() ;
}
