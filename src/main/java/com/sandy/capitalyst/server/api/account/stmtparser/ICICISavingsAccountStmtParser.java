package com.sandy.capitalyst.server.api.account.stmtparser;

import java.io.File ;
import java.sql.Date ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.core.util.StringUtil;
import com.sandy.capitalyst.server.core.xlsutil.XLSRow;
import com.sandy.capitalyst.server.core.xlsutil.XLSRowFilter;
import com.sandy.capitalyst.server.core.xlsutil.XLSUtil;
import com.sandy.capitalyst.server.core.xlsutil.XLSWrapper;
// import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;

public class ICICISavingsAccountStmtParser extends AccountStmtParser {
    
    // static final Logger log = Logger.getLogger( ICICISavingsAccountStmtParser.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    
    private class RowFilter implements XLSRowFilter {
        
        private boolean legendEncountered = false ;
        
        public boolean accept( XLSRow row ) {
            
            String col0Val = row.getCellValue( 0 ) ;
            String col4Val = row.getRawCellValue( 4 ) ;
            
            if( StringUtil.isEmptyOrNull( col4Val ) ) {
                return false ;
            }
            else if( col0Val.startsWith( "Legends" ) ) {
                legendEncountered = true ;
            }
            
            if( legendEncountered ) {
                return false ;
            }
            
            try {
                if( !isContinuationRow(row) ) {
                    Integer.parseInt(col0Val.trim());
                }
                return true ;
            }
            catch( Exception e ){
                return false ;
            }
        }
    }
    
    @Override
    public List<LedgerEntry> parseLedgerEntries( Account account, 
                                                 File file ) 
        throws Exception {
        
        XLSWrapper wrapper = new XLSWrapper( file ) ;
        
        List<LedgerEntry> entries = new ArrayList<>() ;
        List<XLSRow> rows = wrapper.getRows( new RowFilter(), 12, 1, 8 ) ;
        XLSUtil.printRows( rows ) ;
        
        for( XLSRow row : rows ) {
            
            if( isContinuationRow( row ) ) {
                
                String additionalRemarks = row.getCellValue( 4 ) ;
                LedgerEntry lastEntry = entries.get( entries.size()-1 ) ;
                
                lastEntry.addToRemarks( additionalRemarks ) ;
            }
            else {
                entries.add( constructLedgerEntry( account, row ) ) ;
            }
        }
        
        for( LedgerEntry entry : entries ) {
            entry.setRemarks( entry.getRemarks().trim() ) ;
            entry.generateHash() ;
        }
        
        return entries ;
    }
    
    private boolean isContinuationRow( XLSRow row ) {
        
        boolean[] emptyCells = { true, true, true, true, false, true, true, true } ;
        for( int i=0; i<8; i++ ) {
            if( emptyCells[i] ) { 
                if( !StringUtil.isEmptyOrNull( row.getCellValue( i ) ) ) {
                    return false ;
                }
            }
            else {
                if( StringUtil.isEmptyOrNull( row.getCellValue( i ) ) ) {
                    return false ;
                }
            }
        }
        return true ;
    }
    
    private LedgerEntry constructLedgerEntry( Account account, 
                                              XLSRow row ) 
        throws Exception {
        
        LedgerEntry entry = new LedgerEntry() ;
        entry.setAccount( account ) ;
        entry.setValueDate( new Date( SDF.parse( row.getCellValue( 1 ) )
                                         .getTime() ) ) ;
        
        String chequeNum = row.getCellValue( 3 ) ;
        if( StringUtil.isNotEmptyOrNull( chequeNum ) ) {
            chequeNum = chequeNum.trim() ;
            if( !chequeNum.equals( "-" ) ){
                entry.setChequeNumber( chequeNum );
            }
        }
        
        entry.setRemarks( row.getRawCellValue( 4 ) ) ;
        
        float withdrawalAmt = Float.parseFloat( row.getCellValue( 5 ) ) ;
        float depositAmt = Float.parseFloat( row.getCellValue( 6 ) ) ;
        if( depositAmt > 0 ) {
            entry.setAmount( depositAmt ) ;
        }
        else if( withdrawalAmt > 0 ) {
            entry.setAmount( -withdrawalAmt ) ;
        }
        
        entry.setBalance( Float.parseFloat( row.getCellValue( 7 ) ) ) ;
        
        return entry ;
    }
}
