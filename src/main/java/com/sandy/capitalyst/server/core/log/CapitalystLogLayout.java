package com.sandy.capitalyst.server.core.log;

import java.util.Stack ;

import org.apache.log4j.MDC ;
import org.apache.log4j.PatternLayout ;
import org.apache.log4j.spi.LoggingEvent ;

import lombok.Getter ;
import lombok.Setter ;

public abstract class CapitalystLogLayout extends PatternLayout {
    
    public static final String I  = "  " ;
    public static final String I2 = I + I ;
    public static final String I3 = I2 + I ;
    
    private static final String MARKER = "[indent]" ;
    private static final int    MARKER_LEN = MARKER.length() ;
    
    private static final String START_INDENT     = "> " ;
    private static final String START_DEINDENT   = "< " ;
    private static final String START_USE_INDENT = "- " ;
    private static final String START_USE_INDENT2= "-> " ;
    private static final String START_USE_INDENT3= "->> " ;
    private static final String START_RESET      = "<< " ;
    private static final String START_MARK_INDENT= "!> " ;
    private static final String START_INDENT_MARK= ">! " ;
    private static final String MARK_USE_CURRENT = "!- " ;
    
    private static final String END_INDENT      = " >\n" ;
    private static final String END_DEINDENT    = " <\n" ;
    private static final String END_MARK        = " !\n" ;
    private static final String END_RESET       = " <<\n" ;
    private static final String END_MARK_INDENT = " !>\n" ;
    private static final String END_INDENT_MARK = " >!\n" ;
                
    public static class Indent {
        @Getter @Setter String curIndent = "" ;
        @Getter @Setter Stack<String> indentStack = new Stack<>() ;
        
        public void markIndent() {
            indentStack.push( curIndent ) ;
        }
        
        public void resetIndent() {
            if( !indentStack.isEmpty() ) {
                curIndent = indentStack.pop() ;
            }
        }
    }
    
    private String mdcKey = null ;
    
    protected CapitalystLogLayout( String mdcKey ) {
        this.mdcKey = mdcKey ;
    }
    
    @Override
    public String format( LoggingEvent event ) {
        
        String fmtStr = super.format( event ) ;
        
        if( fmtStr.contains( MARKER ) ) {
            // Initialize indent context if none exists.
            getIndent() ;
            
            fmtStr = preProcessIndent( fmtStr ) ;
            fmtStr = postProcessIndent( fmtStr ) ;
        }
        return fmtStr ;
    }
    
    private String preProcessIndent( String fmtStr ) {
        
        int mkrStart = fmtStr.indexOf( MARKER ) ;
        int mkrEnd   = mkrStart + MARKER_LEN ;
        
        String indent = "" ;
        String hint   = "" ;
        
        if( fmtStr.length() > mkrEnd ) {
            
            if( fmtStr.indexOf( START_USE_INDENT3, mkrEnd ) == mkrEnd ) {
                hint = START_USE_INDENT3 ;
                indent = getCurrentIndent() + I2 ;
            }
            else if( fmtStr.indexOf( START_USE_INDENT2, mkrEnd ) == mkrEnd ) {
                hint = START_USE_INDENT2 ;
                indent = getCurrentIndent() + I ;
            }
            else if( fmtStr.indexOf( START_INDENT, mkrEnd ) == mkrEnd ) {
                hint = START_INDENT ;
                indent = incrementIndent() ;
            }
            else if( fmtStr.indexOf( START_DEINDENT, mkrEnd ) == mkrEnd ) {
                hint = START_DEINDENT ;
                indent = decrementIndent() ;
            }
            else if( fmtStr.indexOf( START_USE_INDENT, mkrEnd ) == mkrEnd ) {
                hint = START_USE_INDENT ;
                indent = getCurrentIndent() ;
            }
            else if( fmtStr.indexOf( START_MARK_INDENT, mkrEnd ) == mkrEnd ) {
                hint = START_MARK_INDENT ;
                getIndent().markIndent() ;
                indent = incrementIndent() ;
            }
            else if( fmtStr.indexOf( START_INDENT_MARK, mkrEnd ) == mkrEnd ) {
                hint = START_INDENT_MARK ;
                indent = incrementIndent() ;
                getIndent().markIndent() ;
            }
            else if( fmtStr.indexOf( MARK_USE_CURRENT, mkrEnd ) == mkrEnd ) {
                hint = MARK_USE_CURRENT ;
                indent = getCurrentIndent() ;
                getIndent().markIndent() ;
            }
            else if( fmtStr.indexOf( START_RESET, mkrEnd ) == mkrEnd ) {
                hint = START_RESET ;
                getIndent().resetIndent() ;
                indent = getCurrentIndent() ;
            }
        }
        
        return addIndent( fmtStr, mkrStart, mkrEnd+hint.length(), indent ) ;
    }
    
    private String postProcessIndent( String fmtStr ) {
        
        String endMarker = "" ;
        
        if( fmtStr.endsWith( END_INDENT ) ) {
            incrementIndent() ;
            endMarker = END_INDENT ;
        }
        else if( fmtStr.endsWith( END_DEINDENT ) ) {
            decrementIndent() ;
            endMarker = END_DEINDENT ;
        }
        else if( fmtStr.endsWith( END_MARK ) ) {
            getIndent().markIndent() ;
            endMarker = END_MARK ;
        }
        else if( fmtStr.endsWith( END_RESET ) ) {
            getIndent().resetIndent() ;
            endMarker = END_RESET ;
        }
        else if( fmtStr.endsWith( END_MARK_INDENT ) ) {
            getIndent().markIndent() ;
            incrementIndent() ;
            endMarker = END_MARK_INDENT ;
        }
        else if( fmtStr.endsWith( END_INDENT_MARK ) ) {
            incrementIndent() ;
            getIndent().markIndent() ;
            endMarker = END_INDENT_MARK ;
        }
        
        if( endMarker.equals( "" ) ) {
            return fmtStr ;
        }
        
        return fmtStr.substring( 0, fmtStr.length() - endMarker.length() ) + "\n" ;
    }

    private String getCurrentIndent() {
        return getIndent().curIndent ;
    }

    private Indent getIndent() {
        
        Indent indent = (Indent)MDC.get( this.mdcKey ) ;
        if( indent == null ) {
            indent = new Indent() ;
            MDC.put( this.mdcKey, indent ) ;
        }
        return indent ;
    }

    private String incrementIndent() {
        
        Indent indent = getIndent() ;
        indent.curIndent = indent.curIndent + I ;
        return indent.curIndent ;
    }
    
    private String decrementIndent() {
        
        Indent indent = getIndent() ;
        String curInd = indent.curIndent ;
        
        if( curInd.length() >= I.length() ) {
            curInd = curInd.substring( 0, curInd.length() - I.length() ) ;
            indent.curIndent = curInd ;
        }
        
        return curInd ;
    }

    private String addIndent( String fmtStr, int mkrStart, int mkrEnd,
                              String indent ) {
        
        String start = fmtStr.substring( 0, mkrStart ) ;
        String end   = fmtStr.substring( mkrEnd ) ;
        
        return start + indent + end ;
    }
}
