package com.sandy.capitalyst.server.dao.equity;

import org.springframework.data.repository.CrudRepository ;

public interface EquityHoldingRepo 
    extends CrudRepository<EquityHolding, Integer> {
    
    EquityHolding findByOwnerNameAndSymbolIcici( String ownerName, 
                                                 String symbolIcici ) ;
}
