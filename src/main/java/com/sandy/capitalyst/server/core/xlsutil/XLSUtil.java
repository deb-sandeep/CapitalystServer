package com.sandy.capitalyst.server.core.xlsutil;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class XLSUtil {
    
    private static final Logger log = Logger.getLogger( XLSUtil.class ) ;
    
    public static List<String> getCellValues( Row row, int startCol, int endCol ) {
        
        List<String> cellValues = new ArrayList<>() ;
        
        for( int i=startCol; i<=endCol; i++ ) {
            Cell cell = row.getCell( i ) ;
            
            if( cell == null ) {
                cellValues.add( null ) ;
            }
            else {
                CellType cellType = cell.getCellType() ;
                String cellValue = "XXX" ;
                
                switch( cellType ) {
                    case NUMERIC:
                        cellValue = String.format( "%f", cell.getNumericCellValue() ) ;
                        if( cellValue.endsWith( ".000000" ) ) {
                            cellValue = cellValue.substring( 0, cellValue.length() - ".000000".length() ) ;
                        }
                        break ;
                    case STRING:
                        cellValue = cell.getStringCellValue() ;
                        break ;
                    case BLANK:
                        cellValue = "" ;
                        break ;
                    case BOOLEAN:
                        cellValue = Boolean.toString( cell.getBooleanCellValue() ) ;
                        break ;
                    case ERROR:
                        cellValue = Byte.toString( cell.getErrorCellValue() ) ;
                        break ;
                    case FORMULA:
                        cellValue = cell.getCellFormula() ;
                        break ;
                    case _NONE:
                        cellValue = "" ;
                        break ;
                    default:
                        cellValue = "XXX" ;
                        break ;
                }
                cellValues.add( cellValue ) ;
            }
        }
        return cellValues ;
    }
    
    public static void printRows( List<XLSRow> rows ) {
        if( rows.size() > 0 ) {
            XLSSheetConfig config = rows.get( 0 ).getConfig() ;
            log.debug( config.getHeader() ) ;
        }
        for( XLSRow row : rows ) {
            log.debug( row ) ;
        }
    }
}
