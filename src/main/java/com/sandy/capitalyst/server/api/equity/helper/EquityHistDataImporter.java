package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringReader ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

import lombok.Data ;

/**
 * For a given NSE symbol and a past date range, this one-time class 
 * downloads and imports the end of day data into the database.
 */
public class EquityHistDataImporter {
    
    private static final Logger log = Logger.getLogger( EquityHistDataImporter.class ) ;
    
    private static final SimpleDateFormat REQ_SDF = new SimpleDateFormat( "dd-MM-yyyy" ) ;
    private static final SimpleDateFormat RES_SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private static final String NSE_HISTORIC_DATA_URL = 
            "https://www1.nseindia.com/" + 
            "products/dynaContent/common/productsSymbolMapping.jsp?" + 
            "symbol={symbol}&" + 
            "segmentLink=3&" + 
            "symbolCount=1&" + 
            "series=EQ&" + 
            "dateRange=+&" + 
            "fromDate={fromDate}&" + 
            "toDate={toDate}&" + 
            "dataType=PRICEVOLUME" ;
    
    private static final String DIV_START = "<div id='csvContentDiv' style='display:none;'>" ;
    
    @Data
    public static class ImportResult {
        private int numRecordsFounds = 0 ;
        private int numAdditions = 0 ;
        private int numDeletions = 0 ;
        private int numModified  = 0 ;
        private boolean dataForWrongSymbolReceived = false ;
    }
    
    private String symbol = null ;
    private Date fromDate = null ;
    private Date toDate   = null ;
    
    private ImportResult results = new ImportResult() ;
    
    private HistoricEQDataRepo histRepo = null ;

    public EquityHistDataImporter( String symbol, Date fromDate, Date toDate ) {
        this.symbol   = symbol ;
        this.fromDate = fromDate ;
        this.toDate   = toDate ;
        
        histRepo = getBean( HistoricEQDataRepo.class ) ;
    }
    
    public ImportResult execute() throws Exception {
        
        log.info( "!- Filling historic data for " + symbol + " >" ) ;
        log.info( "-> From date = " + RES_SDF.format( fromDate ) ) ;
        log.info( "-> To date   = " + RES_SDF.format( toDate ) ) ;
        
        try {
            if( fromDate365DaysBeforeToDate() ) {
                throw new IllegalArgumentException( 
                    "From date can't be more than 365 days before the end date" ) ;
            }
            
            String csvContent = getRawHistoricData() ;
            
            if( StringUtil.isNotEmptyOrNull( csvContent ) ) {
                parseAndPopulateHistoricData( csvContent ) ;
            }
            
            log.info( "-> Num records found    = " + results.getNumRecordsFounds() ) ;
            log.info( "-> Num records imported = " + results.getNumAdditions() ) ;
            log.info( "-> Num records modified = " + results.getNumModified() ) ;
            log.info( "-> Num dups deleted     = " + results.getNumDeletions() ) ;
            log.info( "-> Total eod records    = " + histRepo.getNumRecords( symbol ) ) ;
        }
        catch( Exception e ) {
            log.error( "Error updating historic data. " + e.getMessage() ) ;
            throw e ;
        }
        finally {
            log.debug( "-> Completed historic data update <<" ) ;
        }
        return results ;
    }

    private void parseAndPopulateHistoricData( String csvContent )
            throws Exception {
        
        CsvParserSettings settings = new CsvParserSettings() ;
        CsvParser parser = new CsvParser( settings ) ;
        List<String[]> records = null ;
        
        csvContent = csvContent.replace( ":", "\n" ) ;
        records = parser.parseAll( new StringReader( csvContent ) ) ;
        
        results.setNumRecordsFounds( records.size()-1 ) ;
        log.debug( "- Num records found = " + (records.size()-1) );
        
        log.info( "- Importing eod data." ) ;
        if( records.size() > 1 ) {

            String[] firstRecord = records.get( 1 ) ;
            
            if( !firstRecord[0].trim().equals( this.symbol ) ) {
                
                // There is a bizzare scenario where the server returns 
                // EOD for a different symbol. If such a scenario occurs,
                // Don't process this bunch of records.
                log.error( "-> ERROR: Different symbol data obtained. " + 
                           firstRecord[0].trim() ) ;
                
                results.setDataForWrongSymbolReceived( true ) ;
            }
            else {
                for( int i=1; i<records.size(); i++ ) {
                    String[] row = records.get( i ) ;
                    addHistoricRecord( row ) ;
                }
            }
        }
    }

    private boolean fromDate365DaysBeforeToDate() {
        Date maxPastDate = DateUtils.addDays( toDate, -365 ) ;
        return maxPastDate.after( fromDate ) ;
    }
    
    private String getRawHistoricData() throws Exception {
        
        String url = NSE_HISTORIC_DATA_URL
                        .replace( "{symbol}", symbol )
                        .replace( "{fromDate}", REQ_SDF.format( fromDate ) )
                        .replace( "{toDate}", REQ_SDF.format( toDate ) ) ;
        
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        String response = null ;
        String csvContent = null ;
        
        log.info( "- Downloading eod data." ) ;
        response = downloader.getResource( url, "eod-pricevol.txt" ) ;
        log.debug( "-> Done. Response size " + response.length() + " bytes." ) ;
        
        int startIndex = response.indexOf( DIV_START ) ;
        
        if( startIndex != -1 ) {
            startIndex += DIV_START.length() ;
            int endIndex = response.indexOf( "</div>", startIndex ) ;
            csvContent = response.substring( startIndex, endIndex ) ;
            log.debug( "-> Data size = " + csvContent.length() + " bytes." ) ;
        }
        else if( response.contains( "No Records" ) ) {
            log.debug( "-> No records found." ) ;
            csvContent = "" ;
        }
        else {
            log.error( "-> Invalid response from server." ) ;
            log.debug( "->>" + response ) ;
            throw new Exception( "No historic records found." ) ;
        }
        
        return csvContent ;
    }

    private HistoricEQData addHistoricRecord( String[] row ) throws Exception {
        
        HistoricEQData eodData = null ;
        List<HistoricEQData> histRows = null ;
        
        String symbol        = row[0].trim() ;
        Date   date          = RES_SDF.parse   ( row[ 2].trim() ) ;
        float  prevClose     = Float.parseFloat( row[ 3].trim() ) ;
        float  open          = Float.parseFloat( row[ 4].trim() ) ;
        float  high          = Float.parseFloat( row[ 5].trim() ) ;
        float  low           = Float.parseFloat( row[ 6].trim() ) ;
        float  close         = Float.parseFloat( row[ 8].trim() ) ;
        long   totalTradeQty = Long.parseLong  ( row[10].trim() ) ;
        float  totalTradeVal = Float.parseFloat( row[11].trim() ) ;
        long   totalTrades   = Long.parseLong  ( row[12].trim() ) ;
        
        histRows = histRepo.findBySymbolAndDate( symbol, date ) ;
        if( histRows == null || histRows.isEmpty() ) {
            
            eodData = new HistoricEQData() ;
            
            eodData.setSymbol( symbol ) ;
            eodData.setDate( date ) ;
            eodData.setPrevClose( prevClose ) ;
            eodData.setOpen( open ) ;
            eodData.setHigh( high ) ;
            eodData.setLow( low ) ;
            eodData.setClose( close ) ;
            eodData.setTotalTradeQty( totalTradeQty ) ;
            eodData.setTotalTradeVal( totalTradeVal ) ;
            eodData.setTotalTrades( totalTrades ) ;
            
            results.numAdditions++ ;
            histRepo.saveAndFlush( eodData ) ;
        }
        else if( histRows.size() == 1 ) {
            
            HistoricEQData histData = histRows.get( 0 ) ;
            if( histData.getPrevClose() == null || 
                histData.getPrevClose() == 0 ) {
                
                histData.setPrevClose( prevClose ) ;
                results.numModified++ ;
                histRepo.saveAndFlush( histData ) ;
            }
        }
        else {
            deleteDuplicateHistoricData( histRows, prevClose ) ;
        }
        
        return eodData ;
    }
    
    private void deleteDuplicateHistoricData( List<HistoricEQData> candles,
                                              float prevClose ) {
        
        if( candles != null && !candles.isEmpty() && candles.size()>1 ) {
            
            HistoricEQData histData = candles.get( 0 ) ;
            
            if( histData.getPrevClose() == null || 
                histData.getPrevClose() == 0 ) {
                
                histData.setPrevClose( prevClose ) ;
                results.numModified++ ;
                histRepo.saveAndFlush( histData ) ;
            }

            for( int i=1; i<candles.size(); i++ ) {
                histData = candles.get( i ) ;
                results.numDeletions++ ;
                histRepo.delete( histData ) ;
            }
        }
    }
}
