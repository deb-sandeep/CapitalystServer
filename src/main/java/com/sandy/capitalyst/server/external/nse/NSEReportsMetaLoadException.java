package com.sandy.capitalyst.server.external.nse;

public class NSEReportsMetaLoadException extends Exception {

    NSEReportsMetaLoadException( String message ) {
        super( message ) ;
    }

    NSEReportsMetaLoadException( String message, Exception root ) {
        super( message, root ) ;
    }
}
