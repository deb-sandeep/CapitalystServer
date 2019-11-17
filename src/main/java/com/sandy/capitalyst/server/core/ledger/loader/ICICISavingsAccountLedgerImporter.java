package com.sandy.capitalyst.server.core.ledger.loader;

import java.io.File ;
import java.sql.Date ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.account.Account ;
import com.sandy.capitalyst.server.dao.ledger.LedgerEntry ;
import com.sandy.common.util.StringUtil ;
import com.sandy.common.xlsutil.XLSRow ;
import com.sandy.common.xlsutil.XLSRowFilter ;
import com.sandy.common.xlsutil.XLSUtil ;
import com.sandy.common.xlsutil.XLSWrapper ;

public class ICICISavingsAccountLedgerImporter extends LedgerImporter {
    
    static final Logger log = Logger.getLogger( ICICISavingsAccountLedgerImporter.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" ) ;
    
    private class RowFilter implements XLSRowFilter {
        
        private boolean legendEncountered = false ;
        
        public boolean accept( XLSRow row ) {
            
            String col0Val = row.getCellValue( 0 ) ;
            
            if( StringUtil.isEmptyOrNull( col0Val ) ) {
                return false ;
            }
            else if( col0Val.startsWith( "Legends" ) ) {
                legendEncountered = true ;
            }
            
            if( legendEncountered ) {
                return false ;
            }
            
            try {
                Integer.parseInt( col0Val.trim() ) ;
                return true ;
            }
            catch( Exception e ){
                return false ;
            }
        }
    }
    
    @Override
    public void importLedgerEntries( Account account, File file ) 
        throws Exception {
        
        log.debug( "Parsing ledger entries for account " + account.getShortName() ) ;
        XLSWrapper wrapper = new XLSWrapper( file ) ;
        
        List<LedgerEntry> entries = parseLedgerEntries( account, wrapper ) ;
        log.debug( "Ledger entries parsed." ) ;
        log.debug( "\tNum ledger entries = " + entries.size() ) ;
        
        LedgerEntry possibleDup = null ;
        for( LedgerEntry entry : entries ) {
            possibleDup = ledgerRepo.findByHash( entry.getHash() ) ;
            if( possibleDup == null ) {
                log.debug( "\tSaving ledger entry = " + 
                           SDF.format( entry.getValueDate() ) + " - " +
                           entry.getRemarks() + " :: " + 
                           entry.getAmount() ) ; 
                ledgerRepo.save( entry ) ;
            }
            else {
                log.info( "Found a duplicate entry " + entry ) ;
            }
        }
        log.debug( "Ledger entries saved" ) ;
    }
    
    private List<LedgerEntry> parseLedgerEntries( Account account, 
                                                  XLSWrapper xls ) 
        throws Exception {
        
        List<LedgerEntry> entries = new ArrayList<>() ;
        List<XLSRow> rows = xls.getRows( new RowFilter(), 12, 1, 8 ) ;
        XLSUtil.printRows( rows ) ;
        for( XLSRow row : rows ) {
            entries.add( constructLedgerEntry( account, row ) ) ;
        }
        return entries ;
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
        
        entry.setRemarks( row.getCellValue( 4 ) ) ;
        
        Float withdrawalAmt = Float.parseFloat( row.getCellValue( 5 ) ) ;
        Float depositAmt = Float.parseFloat( row.getCellValue( 6 ) ) ;
        if( depositAmt > 0 ) {
            entry.setAmount( depositAmt ) ;
        }
        else if( withdrawalAmt > 0 ) {
            entry.setAmount( -withdrawalAmt ) ;
        }
        
        entry.setBalance( Float.parseFloat( row.getCellValue( 7 ) ) ) ;
        
        entry.generateHash() ;

        return entry ;
    }
}
