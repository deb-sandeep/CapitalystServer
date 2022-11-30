package com.sandy.capitalyst.server.dao.equity.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.equity.SebiPenalty ;

public interface SebiPenaltyRepo 
    extends CrudRepository<SebiPenalty, Integer>{
    
    
    public SebiPenalty findByOrderId( String orderId ) ;
}
