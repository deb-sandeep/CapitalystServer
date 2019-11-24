package com.sandy.capitalyst.server.dao.individual;

import org.springframework.data.repository.CrudRepository ;

public interface IndividualRepo 
    extends CrudRepository<Individual, Integer> {
    
    public Individual findByFirstName( String firstName ) ;
}
