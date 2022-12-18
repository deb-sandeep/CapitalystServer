package com.sandy.capitalyst.server.dao.breeze.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.breeze.BreezeInvocationStats ;

public interface BreezeInvocationStatsRepo 
    extends CrudRepository<BreezeInvocationStats, Integer>, IDCompressor {
    
    BreezeInvocationStats findByDateAndUserNameAndApiId( Date date, 
                                                         String userName, 
                                                         String apiId ) ;
    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   breeze_invocation_stats "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<BreezeInvocationStats> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   breeze_invocation_stats " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
