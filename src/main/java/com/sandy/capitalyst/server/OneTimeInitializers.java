package com.sandy.capitalyst.server;

import java.io.File ;
import java.io.FileInputStream ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.apache.poi.hssf.usermodel.HSSFWorkbook ;
import org.apache.poi.ss.usermodel.Cell ;
import org.apache.poi.ss.usermodel.Row ;
import org.apache.poi.ss.usermodel.Sheet ;
import org.apache.poi.ss.usermodel.Workbook ;
import org.springframework.context.ApplicationContext ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.account.AccountRepo ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.capitalyst.server.dao.ledger.LedgerRepo ;
import com.sandy.common.xlsutil.XLSRow ;
import com.sandy.common.xlsutil.XLSUtil ;
import com.sandy.common.xlsutil.XLSWrapper ;

public class OneTimeInitializers {
    
    private static final Logger log = Logger.getLogger( OneTimeInitializers.class ) ;
    
    private AccountRepo accountRepo = null ;
    private LedgerRepo ledgerRepo = null ;
    
    public OneTimeInitializers() {
        ApplicationContext appCtx = CapitalystServer.getAppContext() ;
        if( appCtx != null ) {
            ledgerRepo = appCtx.getBean( LedgerRepo.class ) ;
            accountRepo = appCtx.getBean( AccountRepo.class ) ;
        }
    }

    public void importHistoricCCEntries() throws Exception {
        
        SimpleDateFormat DF = new SimpleDateFormat( "dd/MM/yy" ) ;
        XLSWrapper wrapper = new XLSWrapper( new File( "/Users/sandeep/temp/CCLog.xls" ) ) ;
        List<XLSRow> rows = wrapper.getRows( 0, 0, 5 ) ;
        Account account = accountRepo.findById( 7671 ).get() ;
        
        int numRowsImported = 0 ;
        
        for( XLSRow row : rows ) {
            LedgerEntry le = new LedgerEntry() ;
            le.setAccount( account ) ;
            le.setAmount( -1 * Float.parseFloat( row.getCellValue( 3 ) ) ) ;
            le.setBalance( Float.parseFloat( row.getCellValue( 5 ) ) ) ;
            le.setNotes( "CC X" + row.getCellValue( 0 ) ) ;
            le.setRemarks( row.getCellValue( 2 ) ) ;
            le.setValueDate( DF.parse( row.getCellValue( 1 ) ) ) ;
            le.generateHash() ;
            
            ledgerRepo.save( le ) ;
            
            account.setBalance( le.getBalance() ) ;
            accountRepo.save( account ) ;
            
            log.debug( "Num rows imported = " + ++numRowsImported ) ;
        }
    }
    
    public void testCCStmtImport() throws Exception {
        
        SimpleDateFormat DF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
        XLSWrapper wrapper = new XLSWrapper( new File( "/Users/sandeep/temp/CCStatementDec19.xls" ) ) ;
        List<XLSRow> rows = wrapper.getRows( 14, 3, 9 ) ;
        
        XLSUtil.printRows( rows ) ;
        
        for( XLSRow row : rows ) {
            Date date = DF.parse( row.getCellValue( 0 ).trim() ) ;
            String remark = row.getCellValue( 1 ).trim() ;
            String amtStr = row.getCellValue( 5 ).trim() ;
            amtStr = amtStr.replace( ",", "" ) ;
            amtStr = amtStr.substring( 0, amtStr.length()-4 ) ;
            
            log.debug( date + "::" + remark + "::" + amtStr ) ;
        }

        XLSUtil.printRows( rows ) ;
    }
    
    public void extractBalance() throws Exception {
        
        File xlsFile = new File( "/Users/sandeep/temp/CCStatementDec19.xls" ) ;
        Workbook workbook = null ;
        FileInputStream fIs = null ;
        
        try {
            fIs = new FileInputStream( xlsFile ) ;
            workbook = new HSSFWorkbook( fIs ) ; 
            Sheet sheet = workbook.getSheetAt( 0 ) ;
            Row row = sheet.getRow( 6 ) ;
            Cell cell = row.getCell( 7 ) ;
            
            String cellVal = cell.getStringCellValue() ;
            boolean isDebit = cellVal.endsWith( "Dr." ) ;
            cellVal = cellVal.substring( 4, cellVal.length()-4 ) ;
            cellVal = cellVal.replace( ",", "" ) ;
            Float val = Float.parseFloat( cellVal ) ;
            
            if( isDebit ) {
                val *= -1 ;
            }
            
            log.debug( val ) ;
        }
        finally {
            fIs.close() ;
            workbook.close() ;
        }
    }
    
    public static void main( String[] args ) throws Exception {
        OneTimeInitializers initializers = new OneTimeInitializers() ;
        initializers.extractBalance() ;
    }
}
