package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.EquityMonitor ;

public interface EquityMonitorRepo 
    extends CrudRepository<EquityMonitor, Integer>{
    
    public EquityMonitor findByIsin( String isin ) ;
    
    public EquityMonitor findBySymbolNse( String symbolNse ) ;
    
    public EquityMonitor findBySymbolIcici( String symbolIcici ) ;
}
