package com.sandy.capitalyst.server.core.api;

public class APIResponse {

    private String message = null ;
    
    public APIResponse( String msg ) {
        this.message = msg ;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }
}
