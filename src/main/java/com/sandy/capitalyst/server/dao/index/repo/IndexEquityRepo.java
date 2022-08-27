package com.sandy.capitalyst.server.dao.index.repo;

import java.util.List ;

import javax.transaction.Transactional ;

import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.index.IndexEquity ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;

public interface IndexEquityRepo 
    extends CrudRepository<IndexEquity, Integer> {
    
    public List<IndexEquity> findByIdxMaster( IndexMaster idxMaster ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    public void deleteByIdxMaster( IndexMaster idx ) ;
    
    @Query( value =   
            "SELECT "
          + "    ie.eqMaster "
          + "FROM "
          + "    IndexEquity ie "
          + "WHERE "
          + "    ie.idxMaster.name = :idxName "
          + "ORDER BY "
          + "    ie.eqMaster.symbol ASC "
    )
    public List<EquityMaster> findByIndex( @Param( "idxName" ) String idxName ) ;
}
