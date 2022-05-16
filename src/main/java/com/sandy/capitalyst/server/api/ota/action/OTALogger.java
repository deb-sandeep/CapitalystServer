package com.sandy.capitalyst.server.api.ota.action;

public interface OTALogger {

    public void addResult( String message ) ;
    public void addResult( Exception e ) ;
}
