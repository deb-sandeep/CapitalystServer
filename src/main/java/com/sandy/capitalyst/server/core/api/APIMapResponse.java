package com.sandy.capitalyst.server.core.api;

import java.util.LinkedHashMap ;
import java.util.Map ;

import lombok.Data ;

@Data
public class APIMapResponse {
    
    private String result = "Success" ;
    private Map<String, Object> body = new LinkedHashMap<>() ;
    
    public APIMapResponse() {}
    
    public APIMapResponse( String result ) {
        this.result = result ;
    }
    
    public APIMapResponse set( String key, Object value ) {
        this.body.put( key, value ) ;
        return this ;
    }
    
    public Object get( String key ) {
        return this.body.get( key ) ;
    }
}
