package com.sandy.capitalyst.server.dao.account.repo;

import java.util.Date ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.account.CorpusSnapshot ;

public interface CorpusSnapshotRepo 
    extends CrudRepository<CorpusSnapshot, Integer> {
 
    CorpusSnapshot findByDate( Date date ) ;
}
