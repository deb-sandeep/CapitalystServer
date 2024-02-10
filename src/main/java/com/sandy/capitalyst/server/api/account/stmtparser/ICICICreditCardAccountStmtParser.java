package com.sandy.capitalyst.server.api.account.stmtparser;

import com.sandy.capitalyst.server.api.account.helper.CCTxnEntry;
import com.sandy.capitalyst.server.core.CapitalystConstants.AccountType;
import com.sandy.capitalyst.server.core.CapitalystConstants.Bank;
import com.sandy.capitalyst.server.core.util.StringUtil;
import com.sandy.capitalyst.server.core.xlsutil.XLSRow;
import com.sandy.capitalyst.server.core.xlsutil.XLSRowFilter;
import com.sandy.capitalyst.server.core.xlsutil.XLSUtil;
import com.sandy.capitalyst.server.core.xlsutil.XLSWrapper;
import com.sandy.capitalyst.server.dao.account.Account;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ICICICreditCardAccountStmtParser extends AccountStmtParser {
    
    static final Logger log = Logger.getLogger( ICICICreditCardAccountStmtParser.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    public static Date EPOCH_START = null ;

    static{try{ EPOCH_START = SDF.parse( "30/12/1899" ); } catch(ParseException ignore){}}

    private static class RowFilter implements XLSRowFilter {
        public boolean accept( XLSRow row ) {
            String col0Val = row.getCellValue( 0 ) ;
            if( StringUtil.isEmptyOrNull( col0Val ) ) {
                return false ;
            }
            else return !col0Val.trim()
                                .startsWith("Transaction Details") ;
        }
    }
    
    private int startRow = 0 ;
    private float balance = 0 ;
    
    @Override
    public List<LedgerEntry> parseLedgerEntries( Account account, 
                                                 File file ) 
        throws Exception {
        
        if( !account.getAccountType().equals( AccountType.CREDIT.name() ) || 
            !account.getBankName().equals( Bank.ICICI.name() ) ) {
            throw new Exception( "Account is not ICICI Credit Card" ) ;
        }
        
        XLSWrapper wrapper = new XLSWrapper( file ) ;
        List<LedgerEntry> entries = new ArrayList<>() ;
        List<XLSRow> rows ;
        
        extractBalanceAndStartRow( file ) ;
        rows = wrapper.getRows(new RowFilter(), startRow, 3, 11 ) ;
        
        XLSUtil.printRows( rows ) ;
        
        for( XLSRow row : rows ) {
            entries.add( constructLedgerEntry( account, row, balance ) ) ;
        }
        
        entries.sort(Comparator.comparing(LedgerEntry::getValueDate)) ;
        
        return entries ;
    }
    
    private void extractBalanceAndStartRow( File xlsFile ) throws Exception {

        try( FileInputStream fIs = new FileInputStream(xlsFile);
             Workbook workbook = new HSSFWorkbook(fIs)) {

            Sheet sheet = workbook.getSheetAt(0);

            this.startRow = getStartRow(sheet);
            this.balance = extractBalance(sheet);
        }
    }
    
    private int getStartRow( Sheet sheet ) {
        
        Row row = sheet.getRow( 12 ) ;
        Cell cell = row.getCell( 2 ) ;
        
        String cellVal = cell.getStringCellValue() ;
        
        if( cellVal.trim().equals( "Transaction Details" ) ) {
            log.debug( "Last statement detected" ) ;
            return 13 ;
        }
        log.debug( "Current statement detected" ) ;
        return 14 ;
    }
    
    public float extractBalance( Sheet sheet ) {
        
        Row row = sheet.getRow( 6 ) ;
        Cell cell = row.getCell( 7 ) ;
        
        String cellVal = cell.getStringCellValue() ;
        boolean isDebit = cellVal.endsWith( "Dr." ) ;
        cellVal = cellVal.substring( 4, cellVal.length()-4 ) ;
        cellVal = cellVal.replace( ",", "" ) ;
        float val = Float.parseFloat( cellVal ) ;
        
        if( isDebit ) {
            val *= -1 ;
        }
        return val ;
    }
    
    private LedgerEntry constructLedgerEntry( Account account, 
                                              XLSRow row, float balance ) {
        
        CCTxnEntry ccTxnEntry = new CCTxnEntry() ;
        
        ccTxnEntry.setCreditCardNumber ( account.getAccountNumber()       ) ;
        ccTxnEntry.setValueDate        ( getDate( row.getCellValue( 0 ) ) ) ;
        ccTxnEntry.setRemarks          ( row.getCellValue( 1 ).trim()     ) ;
        ccTxnEntry.setTxnRefNum        ( row.getCellValue( 8 )            ) ;
        ccTxnEntry.setAmount           ( getAmt( row.getCellValue( 5 ) )  ) ;
        ccTxnEntry.setBalance          ( balance                          ) ;
        
        return ccTxnEntry.convertToLedgerEntry() ;
    }
    
    private Date getDate( String val ) {
        Date retVal ;
        try {
            retVal = SDF.parse( val ) ;
        }
        catch( ParseException pe ) {
            int numDaysSinceEpoch = Integer.parseInt( val ) ;
            retVal = DateUtils.addDays( EPOCH_START, numDaysSinceEpoch ) ;
        }
        return retVal ;
    }
    
    private Float getAmt( String val ) {
        
        String amtStr = val.trim() ;
        boolean isDebit = amtStr.endsWith( "Dr." ) ;
        amtStr = amtStr.replace( ",", "" ) ;
        amtStr = amtStr.substring( 0, amtStr.length()-4 ) ;

        float amt = Float.parseFloat( amtStr ) ;
        amt = isDebit ? -1*amt : amt ;
        
        return amt ;
    }
}
