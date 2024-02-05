package com.sandy.capitalyst.server.core.xlsutil.filter;

import com.sandy.capitalyst.server.core.util.StringUtil;
import com.sandy.capitalyst.server.core.xlsutil.XLSRow;
import com.sandy.capitalyst.server.core.xlsutil.XLSRowFilter;

public abstract class AbstractColFilter implements XLSRowFilter {
    
    protected String colName = null ;
    protected int colIndex = -1 ;
    
    public AbstractColFilter( String colName ) {
        this.colName = colName ;
    }
    
    public AbstractColFilter( int colIndex ) {
        this.colIndex = colIndex ;
    }
    
    protected String getCellStringValue( XLSRow row ) {
        if( colIndex != -1 ) {
            return row.getCellValue( colIndex ) ;
        }
        else if( colName != null ) {
            return row.getCellValue( colName ) ;
        }
        return "XXX" ;
    }

    protected Double getNumericStringValue( XLSRow row ) {
        String strCellVal = getCellStringValue( row ) ;
        if( StringUtil.isEmptyOrNull( strCellVal ) ) {
            return null ;
        }
        return Double.parseDouble( strCellVal ) ;
    }
}
