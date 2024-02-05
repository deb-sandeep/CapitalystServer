package com.sandy.capitalyst.server.core.xlsutil;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class XLSWrapper {
    
    private File xlsFile = null ;
    
    public XLSWrapper( File xlsFile ) throws Exception {
        
        String fileName = xlsFile.getName() ;
        String ext = fileName.substring( fileName.lastIndexOf( "." ) + 1 ) ;
        if( !( ext.equals( "xls" ) || ext.equals( "xlsx" ) ) ) {
            throw new Exception( "File is not an excel file." ) ;
        }
        else if( !xlsFile.exists() ) {
            throw new Exception( "File " + xlsFile.getAbsolutePath() + " doesn't exist." ) ;
        }
        
        this.xlsFile = xlsFile ;
    }
    
    public List<XLSRow> getRows( int startRow, int startCol, int endCol ) 
        throws Exception {
        return this.getRows( null, null, startRow, startCol, endCol ) ;
    }
    
    public List<XLSRow> getRows( XLSRowFilter filter, 
                                 int startRow, int startCol, int endCol ) 
        throws Exception {
        return this.getRows( null, filter, startRow, startCol, endCol ) ;
    }
    
    public List<XLSRow> getRows( String sheetName, 
                                 int startRow, int startCol, int endCol ) 
        throws Exception {
        return this.getRows( sheetName, null, startRow, startCol, endCol ) ;
    }
    
    public List<XLSRow> getRows( String sheetName, XLSRowFilter filter, 
                                 int startRow, int startCol, int endCol ) 
        throws Exception {
        
        List<XLSRow> rows = new ArrayList<>() ;
        Workbook workbook = null ;
        FileInputStream fIs = null ;
        
        try {
            fIs = new FileInputStream( xlsFile ) ;
            if( xlsFile.getName().endsWith( ".xls" ) ) {
                workbook = new HSSFWorkbook( fIs ) ; 
            }
            else {
                workbook = new XSSFWorkbook( fIs ) ;
            }
            
            Sheet sheet = getSheet( workbook, sheetName ) ;
            int numPhyCols = sheet.getRow( startRow ).getLastCellNum() ;
            
            if( endCol == -1 || endCol > numPhyCols ) {
                endCol = numPhyCols-1 ;
            }
            
            XLSSheetConfig sheetConfig = new XLSSheetConfig( sheet, startRow, 
                                                             startCol, endCol ) ;
            
            for( int i=startRow+1; i<=sheetConfig.getNumRows(); i++ ) {
                List<String> cellValues = null ;
                XLSRow row = null ;
                
                cellValues = XLSUtil.getCellValues( sheet.getRow( i ), startCol, endCol ) ;
                row = new XLSRow( cellValues, sheetConfig ) ;
                if( ( filter == null ) || 
                    ( filter != null && filter.accept( row ) ) ) {
                    sheetConfig.updateColSize( row ) ;
                    rows.add( row ) ;
                }
            }
        }
        finally {
            fIs.close() ;
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
