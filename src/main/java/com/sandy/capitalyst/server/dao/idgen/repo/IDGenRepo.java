package com.sandy.capitalyst.server.dao.idgen.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.idgen.IDGen ;

public interface IDGenRepo extends CrudRepository<IDGen, Integer> {
    
    public IDGen findByGenKey( String genKey ) ;
}
