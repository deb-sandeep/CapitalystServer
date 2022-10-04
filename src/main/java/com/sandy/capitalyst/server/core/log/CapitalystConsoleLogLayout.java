package com.sandy.capitalyst.server.core.log;

public class CapitalystConsoleLogLayout extends CapitalystLogLayout {

    public static String MDC_KEY = "CapitalystConsoleLogLayout-MDCKey" ;

    public CapitalystConsoleLogLayout() {
        super( MDC_KEY ) ;
    }
}
