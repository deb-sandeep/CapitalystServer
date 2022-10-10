package com.sandy.capitalyst.server.core.log;

public class CapitalystOTALogLayout extends CapitalystLogLayout {

    public static String MDC_KEY = "CapitalystOTALogLayout-MDCKey" ;

    public CapitalystOTALogLayout() {
        super( MDC_KEY ) ;
    }
}
