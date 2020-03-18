package com.sandy.capitalyst.server.dao.individual.repo;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.individual.Individual ;

public interface IndividualRepo 
    extends CrudRepository<Individual, Integer> {
    
    public Individual findByFirstName( String firstName ) ;
}
