package com.sandy.capitalyst.server.core.util;

import java.util.Stack ;

import org.apache.log4j.Logger ;
import org.apache.log4j.MDC ;

import com.sandy.capitalyst.server.core.util.IndentUtil.IndentCtx ;

public class IndentUtil extends ThreadLocal<IndentCtx>{

    public static final String I  = "  " ;
    public static final String I2 = I + I ;
    public static final String I3 = I2 + I ;
    
    private static String MDC_KEY = "indent" ;
    
    public class IndentCtx {
        Stack<String> indentMarks = new Stack<>() ;
        String curIndent = "" ;
    }
    
    private static IndentUtil instance = new IndentUtil() ; 
    
    @Override
    protected IndentCtx initialValue() {
        return new IndentCtx() ;
    }

    public static void i_mark() {
        IndentCtx ctx = instance.get() ;
        ctx.indentMarks.push( ctx.curIndent ) ;
    }
    
    public static void i_reset() {
        IndentCtx ctx = instance.get() ;
        if( !ctx.indentMarks.isEmpty() ) {
            ctx.curIndent = ctx.indentMarks.pop() ;
            MDC.put( MDC_KEY, ctx.curIndent ) ;
        }
    }
    
    public static String ip() {
        
        IndentCtx ctx = instance.get() ;
        ctx.curIndent = ctx.curIndent + I ;
        MDC.put( MDC_KEY, ctx.curIndent ) ;
        return "" ;
    }
    
    public static String im() {
        IndentCtx ctx = instance.get() ;
        if( ctx.curIndent.length() >= I.length() ) {
            ctx.curIndent = ctx.curIndent
                               .substring( 0, ctx.curIndent
                                                 .length() - I.length() ) ;
        }
        MDC.put( MDC_KEY, ctx.curIndent ) ;
        return "" ;
    }
    
    public static String i1() {
        return I ;
    }

    public static String i2() {
        return I2 ;
    }

    public static void i_clear() {
        IndentCtx ctx = instance.get() ;
        ctx.curIndent = "" ;
        ctx.indentMarks.clear() ;
        MDC.remove( MDC_KEY ) ;
    }
    
    public static void main( String[] args ) {
        
        Logger log = Logger.getLogger( "Test" ) ;
        
        log.debug( "Log 1" ) ;
        log.debug( ip() + "Log 1.1" ) ;
        
        i_mark() ;
        log.debug( ip() + "Log 1.1.1" ) ;
        log.debug( ip() + "Log 1.1.1.1" ) ;
        log.debug( ip() + "Log 1.1.1.1.1" ) ;
        log.debug(        "Log 1.1.1.1.2" ) ;
        i_reset() ;
        
        log.debug( "Log 1.2" ) ;
        log.debug( "Log 1.3" ) ;
    }
}
