package com.sandy.capitalyst.server.core.util;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

public class ListOfStringPrinter {
    
    private static Logger log = Logger.getLogger( ListOfStringPrinter.class ) ;

    public static void printLoS( List<String[]> strList ) {
        new ListOfStringPrinter( strList, false ).print() ;
    }
    
    public static void printLoSWithHeader( List<String[]> strList ) {
        new ListOfStringPrinter( strList, true ).print() ;
    }
    
    private List<String[]> strList = null ;
    private boolean header = false ;
    
    private int numCols = 0 ;
    private int maxColWidth[] = null ;
    
    private ListOfStringPrinter( List<String[]> strList, boolean header ) {
        
        this.strList = strList ;
        this.header = header ;
        
        String[] firstRow = strList.get( 0 ) ;
        
        numCols = firstRow.length ;
        maxColWidth = new int[ numCols ] ;
        
        computeMaxColWidths() ;
    }
    
    private void computeMaxColWidths() {
        
        for( String[] row : strList ) {
            for( int i=0; i<numCols; i++ ) {
                int strLen = row[i].length() ;
                if( strLen > maxColWidth[i] ) {
                    maxColWidth[i] = strLen ;
                }
            }
        }
    }
    
    private void print() {
        log.debug( getHR() );
        for( int i=0; i<strList.size(); i++ ) {
            log.debug( getRowAsString( strList.get( i ) ) ) ;
            if( header && i==0 ) {
                log.debug( getHR() );
            }
        }
        log.debug( getHR() );
    }
    
    private String getRowAsString( String[] row ) {
        StringBuilder sb = new StringBuilder() ;
        for( int i=0; i<numCols; i++ ) {
            sb.append( StringUtils.rightPad( row[i], maxColWidth[i] ) ) ;
            sb.append( " | " ) ;
        }
        return sb.toString() ;
    }
    
    private String getHR() {
        
        StringBuilder sb = new StringBuilder() ;
        for( int i=0; i<numCols; i++ ) {
            sb.append( StringUtils.repeat( "-", maxColWidth[i] ) ) ;
            sb.append( "-+-" ) ;
        }
        return sb.toString() ;
    }
    
    public static void main( String[] args ) {
        
        List<String[]> list = new ArrayList<>() ;
        list.add( new String[]{ "One", "Two", "Three" } ) ;
        list.add( new String[]{ "Un", "Dos", "Tres" } ) ;
        list.add( new String[]{ "Ek", "Do", "Teen" } ) ;
        
        new ListOfStringPrinter( list, false ).print() ;
    }
}
