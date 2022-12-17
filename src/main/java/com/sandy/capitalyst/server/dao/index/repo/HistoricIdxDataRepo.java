package com.sandy.capitalyst.server.dao.index.repo;

import java.util.Date ;
import java.util.List ;

import org.springframework.data.jpa.repository.JpaRepository ;
import org.springframework.data.jpa.repository.Modifying ;
import org.springframework.data.jpa.repository.Query ;
import org.springframework.data.repository.CrudRepository ;
import org.springframework.data.repository.query.Param ;
import org.springframework.transaction.annotation.Transactional ;

import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.index.HistoricIdxData ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;

public interface HistoricIdxDataRepo 
    extends CrudRepository<HistoricIdxData, Integer>,
            JpaRepository<HistoricIdxData, Integer>,
            IDCompressor {
    
    @Query( value = 
            "SELECT COUNT(h) "
          + "FROM HistoricIdxData h "
          + "WHERE "
          + "   h.index = :index "
    )
    int getNumRecords( @Param( "index" ) IndexMaster index ) ;

    @Query( value = 
            "SELECT h "
          + "FROM HistoricIdxData h "
          + "WHERE "
          + "   h.index = :index AND "
          + "   h.date = :date "
          + "ORDER BY "
          + "   h.date ASC "
    )
    HistoricIdxData findByIndexAndDate( @Param( "index" ) IndexMaster index,
                                        @Param( "date" ) Date date ) ;

    @Query( nativeQuery = true,
            value = 
            "SELECT "  
          + "   * " 
          + "FROM " 
          + "   historic_idx_data "
          + "ORDER BY " 
          + "   id ASC "
          + "LIMIT ?2 OFFSET ?1 "
    )
    List<HistoricIdxData> getBatchOfRecords( Integer offset, Integer numRecords ) ;

    @Transactional
    @Modifying( clearAutomatically = true )
    @Query( nativeQuery = true,
            value = 
            "UPDATE "  
          + "   historic_idx_data " 
          + "SET " 
          + "   id = ?2 "
          + "WHERE " 
          + "   id = ?1 "
    )
    void changeID( Integer oldId, Integer newId ) ;
}
