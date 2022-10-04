package com.sandy.capitalyst.server.core.log;

public class CapitalystFileLogLayout extends CapitalystLogLayout {
    
    public static String MDC_KEY = "CapitalystFileLogLayout-MDCKey" ;

    public CapitalystFileLogLayout() {
        super( MDC_KEY ) ;
    }
}
