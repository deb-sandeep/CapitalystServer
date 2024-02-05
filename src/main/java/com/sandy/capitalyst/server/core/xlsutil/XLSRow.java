package com.sandy.capitalyst.server.core.xlsutil;

import com.sandy.capitalyst.server.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class XLSRow {
    
    private XLSSheetConfig config = null ;
    private List<String> cellValues = null ;

    public XLSRow( List<String> cellValues, XLSSheetConfig sheetConfig ) {
        this.config = sheetConfig ;
        this.cellValues = cellValues ;
    }
    
    public XLSSheetConfig getConfig() {
        return this.config ;
    }
    
    public String getCellValue( int index ) {
        String cellVal = cellValues.get( index ) ;
        return StringUtil.isNotEmptyOrNull( cellVal ) ? cellVal.trim() : "" ;
    }
    
    public String getRawCellValue( int index ) {
        return cellValues.get( index ) ;
    }
    
    public String getCellValue( String colName ) {
        String cellVal = cellValues.get( config.getColIndex( colName ) ) ;
        return StringUtil.isNotEmptyOrNull( cellVal ) ? cellVal.trim() : "" ;
    }
    
    public String getRawCellValue( String colName ) {
        return getRawCellValue( config.getColIndex( colName ) ) ;
    }
    
    public String toString() {
        StringBuilder buffer = new StringBuilder() ;
        for( int i=0; i<cellValues.size()-1; i++ ) {
            int colSize = this.config.getColSize( i ) ;
            buffer.append( StringUtils.rightPad( getCellValue( i ), colSize ) ) 
                  .append( " | " ) ;
        }
        buffer.append( getCellValue( cellValues.size()-1 ) ) ;
        return buffer.toString() ;
    }
}
