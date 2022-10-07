package com.sandy.capitalyst.server.breeze;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.commons.lang.exception.ExceptionUtils ;
import org.apache.log4j.Logger ;

import lombok.Data ;
import lombok.EqualsAndHashCode ;

@Data
@EqualsAndHashCode( callSuper = false ) 
public class BreezeException extends Exception {
    
    static final Logger log = Logger.getLogger( BreezeException.class ) ;
    
    private static final long serialVersionUID = 1L ;
    
    public static enum Type {
        SERVER_ERROR,
        API_DAY_LIMIT_EXCEED,
        API_MIN_LIMIT_EXCEED,
        APP_EXCEPTION,
        SESSION_ERROR,
        NO_PORTFOLIO
    } ;
    
    private class OriginLine {
        
        String className  = null ;
        String methodName = null ;
        int    lineNumber = -1 ;
        String fileName   = null ;
        
        OriginLine( StackTraceElement e ) {
            className  = e.getClassName() ;
            methodName = e.getMethodName() ;
            lineNumber = e.getLineNumber() ;
            fileName   = e.getFileName() ;
        }
    } ;
    
    private Type      type           = null ;
    private int       httpStatusCode = 0 ;
    private Exception rootException  = null ;
    
    private List<OriginLine> originLines = new ArrayList<>() ;
    
    public static BreezeException sessionError( String userName,
                                                String scenario,
                                                String msg ) {
        
        BreezeException e = new BreezeException( Type.SESSION_ERROR,
                                    "Session error for " + userName + ". " +
                                    "Scenario = " + scenario + ". " +
                                    "Detail = " + msg ) ;
        return e ;
    }
    
    public static BreezeException dayRateExceed( String userName ) {
    
        BreezeException e = new BreezeException( Type.API_DAY_LIMIT_EXCEED,
                                    "API day limit exceeded for " + userName ) ;
        return e ;
    }
    
    public static BreezeException minRateExceed( String userName ) {
        
        BreezeException e = new BreezeException( Type.API_MIN_LIMIT_EXCEED,
                                    "API minute limit exceeded for " + userName ) ;
        return e ;
    }
    
    public static BreezeException appException( String msg ) {
        
        BreezeException e = new BreezeException( Type.APP_EXCEPTION, 
                                                 msg ) ;
        return e ;
    }
    
    public static BreezeException appException( Exception cause ) {
        
        BreezeException e = new BreezeException( Type.APP_EXCEPTION, 
                                                 cause.getMessage() ) ;
        e.setRootException( cause ) ;
        return e ;
    }
    
    public static BreezeException appException( String msg, Exception cause ) {
        
        BreezeException e = new BreezeException( Type.APP_EXCEPTION, 
                                                 msg ) ;
        e.setRootException( cause ) ;
        return e ;
    }
    
    public static BreezeException httpError( int code, String bodyContent ) {
        
        BreezeException e = new BreezeException( Type.SERVER_ERROR, 
                                                 bodyContent ) ;
        e.setHttpStatusCode( code ) ; 
        
        return e ;
    }
    
    public static BreezeException serverError( String message ) {
        
        BreezeException e = new BreezeException( Type.SERVER_ERROR, 
                                                 message ) ;
        e.setHttpStatusCode( 500 ) ;
        return e ;
    }
    
    public static BreezeException noPortfolio() {
        
        BreezeException e = new BreezeException( Type.NO_PORTFOLIO, 
                                                 "Empty portfolio." ) ;
        e.setHttpStatusCode( 500 ) ;
        return e ;
    }
    
    private BreezeException( Type type, String msg ) {
        
        super( msg ) ;
        
        this.type    = type ;
        this.extractOrigin() ;
    }

    private void extractOrigin() {
        
        Exception exception = new Exception() ;
        StackTraceElement[] elements = exception.getStackTrace() ;
        
        for( StackTraceElement e : elements ) {
            originLines.add( new OriginLine( e ) ) ;
        }
    }
    
    public String toString() {
        
        StringBuilder sb = new StringBuilder( "BreezeException [\n" ) ;
        //          12345678901234567
        sb.append( "   Type          = " + type         + "\n" ) ;
        sb.append( "   Msg           = " + getMessage() + "\n" ) ;
        
        if( Breeze.config().isPrintExceptionOrigins() ) {
            sb.append( "   Origins       = \n" ) ;
            originLines.subList( 3, originLines.size() ).forEach( origin -> {
                sb.append( "    " + origin.className  + "::" + 
                                    origin.methodName + " (" + 
                                    origin.fileName   + ":"  + 
                                    origin.lineNumber + ")\n" ) ;
            } ) ;
        } ;
        
        switch( type ) {
            case SERVER_ERROR:
                sb.append( "   Status Code = " + httpStatusCode + "\n" ) ;
                break ;
            case APP_EXCEPTION:
                sb.append( "   Exception     = \n" + ExceptionUtils.getStackTrace( rootException ) ) ;
                break ;
            default:
                break ;
        }
        
        sb.append( "]" ) ;
        
        return sb.toString() ;
    }
}
