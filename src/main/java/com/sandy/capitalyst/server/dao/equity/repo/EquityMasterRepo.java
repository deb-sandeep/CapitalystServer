package com.sandy.capitalyst.server.dao.equity.repo;

import java.util.List ;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;

public interface EquityMasterRepo 
    extends CrudRepository<EquityMaster, Integer>,
            JpaRepository<EquityMaster, Integer>{
    
    public EquityMaster findByIsin( String isin ) ;
    
    public EquityMaster findBySymbol( String symbol ) ;
    
    public EquityMaster findBySymbolIcici( String symbolIcici ) ;
    
    @Query( nativeQuery = true,
            value =   
            "SELECT "
          + "    em.symbol "
          + "FROM "
          + "    equity_master em "
          + "WHERE "
          + "    em.is_etf = 1 "
    )
    public List<String> findETFStocks() ;
}
