package com.sandy.capitalyst.server.breeze.internal;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Data ;

@Data
public class BreezeAPIResponse<T> {

    private int status = 0 ;
    private String error = null ;
    private List<T> entities = new ArrayList<>() ;
    
    public void addEntity( T t ) {
        entities.add( t ) ;
    }
}
