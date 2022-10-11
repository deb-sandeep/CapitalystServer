package com.sandy.capitalyst.server.core.log;

import org.apache.log4j.MDC ;

import com.sandy.capitalyst.server.core.log.CapitalystLogLayout.Indent ;

public class IndentUtil {
    
    @SuppressWarnings( "unchecked" )
    public static void i_mark() {
        
        MDC.getContext().values().forEach( item -> {
            Indent indent = ( Indent )item ;
            indent.markIndent() ;
        } ) ;
    }
    
    @SuppressWarnings( "unchecked" )
    public static void i_reset() {
        
        MDC.getContext().values().forEach( item -> {
            Indent indent = ( Indent )item ;
            indent.resetIndent() ;
        } ) ;
    }
    
    @SuppressWarnings( "unchecked" )
    public static void i_clear() {
        
        MDC.getContext().values().forEach( item -> {
            Indent indent = ( Indent )item ;
            indent.clearIndent() ;
        } ) ;
    }
}
