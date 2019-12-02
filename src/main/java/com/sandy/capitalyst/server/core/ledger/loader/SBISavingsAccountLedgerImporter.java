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
import com.univocity.parsers.tsv.TsvParser ;
import com.univocity.parsers.tsv.TsvParserSettings ;

public class SBISavingsAccountLedgerImporter extends LedgerImporter {
    
    static final Logger log = Logger.getLogger( SBISavingsAccountLedgerImporter.class ) ;
    
    private static final SimpleDateFormat VALUE_DT_SDF = 
                                        new SimpleDateFormat( "dd MMM yyyy" ) ;
    
    @Override
    public List<LedgerEntry> parseLedgerEntries( Account account, 
                                                 File file ) 
        throws Exception {
        
        List<LedgerEntry> entries = new ArrayList<>() ;
        List<String[]> rawTupules = getLedgerRawTupules( file ) ;
        for( String[] tupule : rawTupules ) {
            LedgerEntry entry = buildLedgerEntry( account, tupule ) ; 
            log.debug( toString( entry ) );
            entries.add( entry ) ;
        }
        return entries ;
    }
    
    public String toString( LedgerEntry entry ) {
        
        StringBuffer buffer = new StringBuffer() ;
        buffer.append( "LedgerEntry [" ).append( "\n" )
              .append( "  Value date = " + VALUE_DT_SDF.format( entry.getValueDate() ) ).append( "\n" )
              .append( "  Remarks = " + entry.getRemarks() ).append( "\n" )
              .append( "  Amount = " + entry.getAmount() ).append( "\n" )
              .append( "]" ) ;
        return buffer.toString() ;
    }
    
    private List<String[]> getLedgerRawTupules( File file ) {

        List<String[]> ledgerRowTupules = new ArrayList<>() ;
        TsvParserSettings settings = new TsvParserSettings() ;
        TsvParser tsvParser = new TsvParser( settings ) ;
        List<String[]> tsvRows = tsvParser.parseAll( file ) ;
        
        for( String[] rowContents : tsvRows ) {
            if( isValidLedgerRow( rowContents ) ) {
                ledgerRowTupules.add( rowContents ) ;
            }
        }
        return ledgerRowTupules ;
    }
    
    private boolean isValidLedgerRow( String[] rowContents ) {
        try {
            VALUE_DT_SDF.parse( rowContents[0] ) ;
        }
        catch( Exception e ) {
            return false ;
        }
        return true ;
    }
    
    // Ignore cheque number for SBI upload. SBI uses this column
    // for both NEFT and Cheque details. More over Cheque is never used
    // for SBI in our case.
    private LedgerEntry buildLedgerEntry( Account account, String[] tupule ) 
        throws Exception {
        
        LedgerEntry entry = new LedgerEntry() ;
        entry.setAccount( account ) ;
        entry.setValueDate( new Date( VALUE_DT_SDF.parse( tupule[1] ).getTime() ) ) ;
        
        entry.setRemarks( tupule[2] ) ;
        
        Float withdrawalAmt = getFloatValue( tupule[4] ) ;
        Float depositAmt = getFloatValue( tupule[5] ) ;
        if( depositAmt > 0 ) {
            entry.setAmount( depositAmt ) ;
        }
        else if( withdrawalAmt > 0 ) {
            entry.setAmount( -withdrawalAmt ) ;
        }
        
        entry.setBalance( getFloatValue( tupule[6] ) ) ;
        entry.generateHash() ;
        
        return entry ;
    }
    
    private Float getFloatValue( String input ) {
        if( StringUtil.isNotEmptyOrNull( input ) ) {
            input = input.replace( ",", "" ) ;
            return Float.parseFloat( input ) ;
        }
        return Float.valueOf( 0 ) ;
    }
}
