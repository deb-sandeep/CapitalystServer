package com.sandy.capitalyst.server.api.account.stmtparser;

import java.io.File ;
import java.io.FileInputStream ;
import java.sql.Date ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.apache.poi.hssf.usermodel.HSSFWorkbook ;
import org.apache.poi.ss.usermodel.Cell ;
import org.apache.poi.ss.usermodel.Row ;
import org.apache.poi.ss.usermodel.Sheet ;
import org.apache.poi.ss.usermodel.Workbook ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.common.util.StringUtil ;
import com.sandy.common.xlsutil.XLSRow ;
import com.sandy.common.xlsutil.XLSRowFilter ;
import com.sandy.common.xlsutil.XLSUtil ;
import com.sandy.common.xlsutil.XLSWrapper ;

public class POSavingsAccountStmtParser extends AccountStmtParser {
    
    static final Logger log = Logger.getLogger( POSavingsAccountStmtParser.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) ;
    
    private class RowFilter implements XLSRowFilter {
        
        public boolean accept( XLSRow row ) {
            
            String col0Val = row.getCellValue( 0 ) ;
            String col5Val = row.getRawCellValue( 5 ) ;
            
            if( StringUtil.isNotEmptyOrNull( col0Val ) || 
                StringUtil.isNotEmptyOrNull( col5Val ) ) {
                return true ;
            }
            return false ;
        }
    }
    
    @Override
    public List<LedgerEntry> parseLedgerEntries( Account account, 
                                                 File file ) 
        throws Exception {
        
        checkValidStatementFile( account, file ) ;
        
        XLSWrapper wrapper = new XLSWrapper( file ) ;
        
        List<LedgerEntry> entries = new ArrayList<>() ;
        List<XLSRow> rows = wrapper.getRows( new RowFilter(), 10, 2, 8 ) ;
        XLSUtil.printRows( rows ) ;
        
        if( rows.size() > 0 ) {
            if( rows.size()%2 != 0 ) {
                throw new Exception( "Number of rows not a multiple of 2." ) ;
            }
            else {
                for( int i=0; i<rows.size(); i+=2 ) {
                    XLSRow txnDateRow = rows.get( i ) ;
                    XLSRow amtRow     = rows.get( i+1 ) ;
                    
                    String date    = txnDateRow.getCellValue( 0 ) ;
                    String remarks = txnDateRow.getCellValue( 2 ) ;
                    String txnType = amtRow.getCellValue( 5 ) ;
                    String amt     = amtRow.getCellValue( 6 ).replaceAll( ",", "" ) ;
                    
                    LedgerEntry entry = constructEntry( account, date, remarks, 
                                                        txnType, amt ) ;
                    
                    entries.add( entry ) ;
                }
            }
        }
        
        return entries ;
    }
    
    private LedgerEntry constructEntry( Account account,
                                        String date, String remarks,
                                        String txnType, String amtStr ) 
        throws Exception {
        
        LedgerEntry entry = new LedgerEntry() ;
        
        entry.setAccount( account ) ;
        entry.setValueDate( new Date( SDF.parse( date ).getTime() ) ) ;
        entry.setRemarks( remarks ) ;

        Float amt = Float.parseFloat( amtStr ) ;
        if( !txnType.equals( "Cr." ) ) {
            amt *= -1 ;
        }
        entry.setAmount( amt ) ;
        entry.setBalance( 0 ) ;
        
        entry.generateHash() ;

        return entry ;
    }

    private void checkValidStatementFile( Account account,
                                          File xlsFile ) 
        throws Exception {
        
        Workbook workbook = null ;
        FileInputStream fIs = null ;
        
        try {
            fIs = new FileInputStream( xlsFile ) ;
            workbook = new HSSFWorkbook( fIs ) ; 
            Sheet sheet = workbook.getSheetAt( 0 ) ;
            
            Row row = sheet.getRow( 9 ) ;
            Cell cell = row.getCell( 1 ) ;
            
            String cellVal = cell.getStringCellValue() ;
            if( StringUtil.isEmptyOrNull( cellVal ) ) {
                throw new Exception( "Invalid file. Validation row is empty." ) ;
            }
            else {
                cellVal = cellVal.trim() ;
                if( !cellVal.startsWith( "Transactions List" ) ) {
                    throw new Exception( "Invalid file. Validation row does " + 
                                         "not start with 'Transaction List'" ) ;
                }
                
                if( !cellVal.endsWith( account.getAccountNumber() ) ) {
                    throw new Exception( "Invalid file. Validation row does " + 
                                         "not end with right account number." ) ;
                }
            }
        }
        finally {
            fIs.close() ;
            workbook.close() ;
        }
    }
}
