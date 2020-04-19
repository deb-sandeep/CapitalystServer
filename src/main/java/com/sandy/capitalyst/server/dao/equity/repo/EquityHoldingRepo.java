package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;

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
    
    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( 
            "UPDATE " 
          + "   EquityHolding eh "
          + "SET "
          + "   eh.isin = :isin " 
          + "WHERE "
          + "   eh.symbolNse =:symbol ")
    void updateIsin( @Param( "isin"   ) String isin, 
                     @Param( "symbol" ) String symbol ) ;
}
