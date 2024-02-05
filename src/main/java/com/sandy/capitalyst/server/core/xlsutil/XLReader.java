package com.sandy.capitalyst.server.core.xlsutil;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class XLReader {

    public List<List<String>> getRows( File file, String sheetName, 
                                       int startRow, int startCol, int endCol )
            throws Exception {

        List<List<String>> rows = new ArrayList<>() ;
        Workbook workbook = null ;
        FileInputStream fIs = null ;

        try {
            fIs = new FileInputStream( file ) ;
            workbook = new XSSFWorkbook( fIs ) ;

            Sheet sheet = getSheet( workbook, sheetName ) ;
            int numPhyCols = sheet.getRow( startRow ).getLastCellNum() ;
            int numRows = sheet.getLastRowNum() ;

            if( endCol == -1 || endCol > numPhyCols ) {
                endCol = numPhyCols - 1 ;
            }

            for ( int i = startRow + 1; i<= numRows; i++ ) {
                List<String> cellValues = null ;
                cellValues = XLSUtil.getCellValues( sheet.getRow( i ), startCol, endCol ) ;
                rows.add( cellValues ) ;
            }
        } 
        finally {
            if( fIs != null ) {
                fIs.close() ;
            }
        }

        return rows ;
    }
    
    public List<List<String>> getRows( File file, String sheetName, 
                                       int startRow, int startCol, int endCol,
                                       String... filteredColNames ) 
        throws Exception {
        
        List<List<String>> rows = new ArrayList<>() ;
        Workbook           workbook = null ;
        FileInputStream    fIs = null ;
        List<String>       colNames = null ;
        boolean[]          selectedColFlags = null ;

        try {
            fIs = new FileInputStream( file ) ;
            workbook = new XSSFWorkbook( fIs ) ;

            Sheet sheet = getSheet( workbook, sheetName ) ;
            int numPhyCols = sheet.getRow( startRow ).getLastCellNum() ;
            int numRows = sheet.getLastRowNum() ;

            if( endCol == -1 || endCol > numPhyCols ) {
                endCol = numPhyCols - 1 ;
            }
            
            colNames = XLSUtil.getCellValues( sheet.getRow( startRow ), startCol, endCol ) ;
            selectedColFlags = new boolean[ colNames.size() ] ;
            
            for( int i=0; i<colNames.size(); i++ ) {
                if( filteredColNames == null ) {
                    selectedColFlags[i] = true ;
                }
                else {
                    String colName = colNames.get( i ) ;
                    for( String filteredColName : filteredColNames ) {
                        if( colName.trim().equals( filteredColName.trim() ) ) {
                            selectedColFlags[i] = true ;
                        }
                    }
                }
            }

            for ( int i = startRow + 1; i<= numRows; i++ ) {
                List<String> cellValues = null ;
                List<String> selectedCellValues = new ArrayList<>() ;
                
                cellValues = XLSUtil.getCellValues( sheet.getRow( i ), startCol, endCol ) ;
                
                for( int j=0; j<selectedColFlags.length; j++ ) {
                    if( selectedColFlags[j] ) {
                        selectedCellValues.add( cellValues.get( j ) ) ;
                    }
                }
                
                rows.add( selectedCellValues ) ;
            }
        } 
        finally {
            if( fIs != null ) {
                fIs.close() ;
            }
        }

        return rows ;
    }

    private Sheet getSheet( Workbook workbook, String sheetName ) {

        Sheet sheet = null ;
        if( sheetName != null ) {
            sheet = workbook.getSheet( sheetName ) ;
        } 
        else {
            sheet = workbook.getSheetAt( 0 ) ;
        }
        return sheet ;
    }
}
