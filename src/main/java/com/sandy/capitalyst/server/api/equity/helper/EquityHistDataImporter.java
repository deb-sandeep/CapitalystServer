package com.sandy.capitalyst.server.api.equity.helper;

import java.io.StringReader;
import java.text.SimpleDateFormat ;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.capitalyst.server.core.util.CookieUtil;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import jdk.jfr.ContentType;
import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

import lombok.Data ;
import org.springframework.web.multipart.MultipartFile;

/**
 * For a given NSE symbol and a past date range, this one-time class 
 * downloads and imports the end of day data into the database.
 */
public class EquityHistDataImporter {
    
    private static final Logger log = Logger.getLogger( EquityHistDataImporter.class ) ;
    
    private static final SimpleDateFormat REQ_SDF = new SimpleDateFormat( "dd-MM-yyyy" ) ;
    private static final SimpleDateFormat RES_SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private static final String NSE_HISTORIC_DATA_URL =
            "https://www.nseindia.com/api/historical/securityArchives?" +
            "from={fromDate}&" +
            "to={toDate}&" +
            "symbol={symbol}&" +
            "dataType=priceVolume&" +
            "series=EQ" ;
    
    @Data
    public static class ImportResult {
        
        private String symbol = null ;
        private String fileName = null ;
        private int numRecordsFounds = 0 ;
        private int numAdditions = 0 ;
        private int numDeletions = 0 ;
        private int numModified  = 0 ;
        private boolean dataForWrongSymbolReceived = false ;
    }

    private static class HistoricEODRecord {
        String symbol        = null ;
        Date   date          = null ;
        float  prevClose     = 0.0F ;
        float  open          = 0.0F ;
        float  high          = 0.0F ;
        float  low           = 0.0F ;
        float  close         = 0.0F ;
        long   totalTradeQty = 0L ;
        float  totalTradeVal = 0.0F ;
        long   totalTrades   = 0L ;

        static HistoricEODRecord build( JsonNode node ) throws Exception {

            HistoricEODRecord record = new HistoricEODRecord() ;

            record.symbol        = node.get( "CH_SYMBOL" ).asText().trim() ;
            record.date          = RES_SDF.parse( node.get( "mTIMESTAMP"    ).asText() ) ;
            record.prevClose     = (float)node.get( "CH_PREVIOUS_CLS_PRICE" ).asDouble() ;
            record.open          = (float)node.get( "CH_OPENING_PRICE"      ).asDouble() ;
            record.high          = (float)node.get( "CH_TRADE_HIGH_PRICE"   ).asDouble() ;
            record.low           = (float)node.get( "CH_TRADE_LOW_PRICE"    ).asDouble() ;
            record.close         = (float)node.get( "CH_CLOSING_PRICE"      ).asDouble() ;
            record.totalTradeQty = (long )node.get( "CH_TOT_TRADED_QTY"     ).asDouble() ;
            record.totalTradeVal = (float)node.get( "CH_TOT_TRADED_VAL"     ).asDouble() ;
            record.totalTrades   = (long )node.get( "CH_TOTAL_TRADES"       ).asDouble() ;

            return record ;
        }

        static HistoricEODRecord build( String[] row ) throws Exception {

            HistoricEODRecord record = new HistoricEODRecord() ;

            record.symbol        = row[0].trim() ;
            record.date          = RES_SDF.parse   ( row[ 2] ) ;
            record.prevClose     = Float.parseFloat( decomma( row[ 3] ) ) ;
            record.open          = Float.parseFloat( decomma( row[ 4] ) ) ;
            record.high          = Float.parseFloat( decomma( row[ 5] ) ) ;
            record.low           = Float.parseFloat( decomma( row[ 6] ) ) ;
            record.close         = Float.parseFloat( decomma( row[ 8] ) ) ;
            record.totalTradeQty = Long.parseLong  ( decomma( row[10] ) ) ;
            record.totalTradeVal = Float.parseFloat( decomma( row[11] ) ) ;
            record.totalTrades   = Long.parseLong  ( decomma( row[12] ) ) ;

            return record ;
        }

        private static String decomma( String input ) {
            return input.replaceAll( ",", "" ).trim() ;
        }
    }
    
    private HistoricEQDataRepo histRepo = null ;

    public EquityHistDataImporter( HistoricEQDataRepo repo ) {
        histRepo = repo ;
    }

    public ImportResult importFromServer( String symbol, Date fromDate, Date toDate )
        throws Exception {

        Date maxPastDate = DateUtils.addDays( toDate, -60 ) ;
        if( maxPastDate.after( fromDate ) ) {
            throw new IllegalArgumentException(
                    "From date can't be more than 60 days before the end date" ) ;
        }

        log.info( "!- Filling historic data for " + symbol + " >" ) ;
        log.info( "-> From date = " + RES_SDF.format( fromDate ) ) ;
        log.info( "-> To date   = " + RES_SDF.format( toDate ) ) ;

        String jsonContent = getHistoricDataFromServer( symbol, fromDate, toDate ) ;

        List<HistoricEODRecord> records = new ArrayList<>() ;
        ObjectMapper objMapper = new ObjectMapper() ;
        JsonNode jsonRoot  = objMapper.readTree( jsonContent ) ;

        JsonNode dataNode  = jsonRoot.get( "data" ) ;
        for( int i=0; i<dataNode.size(); i++ ) {
            JsonNode jsonNode = dataNode.get( i ) ;
            records.add( HistoricEODRecord.build( jsonNode ) ) ;
        }

        return importHistoricData( symbol, records ) ;
    }

    private String getHistoricDataFromServer( String symbol, Date fromDate, Date toDate )
            throws Exception {

        String url = NSE_HISTORIC_DATA_URL
                .replace( "{symbol}", symbol )
                .replace( "{fromDate}", REQ_SDF.format( fromDate ) )
                .replace( "{toDate}", REQ_SDF.format( toDate ) ) ;

        log.info( "- Downloading eod data." ) ;
        log.debug( "- URL = " + url ) ;

        HTTPResourceDownloader httpClient = HTTPResourceDownloader.instance() ;
        Map<String, String> cookies = CookieUtil.loadNSECookies() ;

        String response = httpClient.getResource( url, "eod-pricevol.txt", cookies ) ;

        log.debug( "-> Done. Response size " + response.length() + " bytes." ) ;

        return response ;
    }

    public ImportResult importCSVData( String csvContent )
            throws Exception {

        CsvParserSettings settings = new CsvParserSettings() ;
        CsvParser parser = new CsvParser( settings ) ;

        List<String[]> csvRecords = parser.parseAll( new StringReader( csvContent ) ) ;
        List<HistoricEODRecord> records = new ArrayList<>() ;

        log.info( "-> Importing eod data." ) ;
        String symbol = null ;

        if( csvRecords.size() > 1 ) {
            String[] firstRecord = csvRecords.get( 1 ) ;
            symbol = firstRecord[0].trim() ;

            for( int i=1; i<csvRecords.size(); i++ ) {
                records.add( HistoricEODRecord.build( csvRecords.get(i) ) ) ;
            }
            return importHistoricData( symbol, records ) ;
        }
        return null ;
    }
    
    private ImportResult importHistoricData( String symbol, List<HistoricEODRecord> records )
            throws Exception {

        ImportResult results = new ImportResult() ;

        try {
            results.setNumRecordsFounds( records.size() ) ;
            results.setSymbol( symbol ) ;

            log.debug( "-> Num records found = " + (records.size()-1) );

            if( records.size() > 1 ) {

                log.info( "-> Importing eod data." ) ;
                HistoricEODRecord firstRecord = records.get( 0 ) ;

                if( symbol !=null && !firstRecord.symbol.equals( symbol ) ) {

                    // There is a bizzare scenario where the server returns
                    // EOD for a different symbol. If such a scenario occurs,
                    // Don't process this bunch of records.
                    log.error( "-> ERROR: Different symbol data obtained. " +
                            firstRecord.symbol ) ;
                    results.setDataForWrongSymbolReceived( true ) ;
                }
                else {
                    records.forEach( r -> addHistoricRecord( r, results ) ) ;
                }
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

    private void addHistoricRecord( HistoricEODRecord record, ImportResult results ) {
        
        HistoricEQData eodData = null ;
        List<HistoricEQData> histRows = null ;

        histRows = histRepo.findBySymbolAndDate( record.symbol, record.date ) ;
        if( histRows == null || histRows.isEmpty() ) {
            
            eodData = new HistoricEQData() ;
            
            eodData.setSymbol( record.symbol ) ;
            eodData.setDate( record.date ) ;
            eodData.setPrevClose( record.prevClose ) ;
            eodData.setOpen( record.open ) ;
            eodData.setHigh( record.high ) ;
            eodData.setLow( record.low ) ;
            eodData.setClose( record.close ) ;
            eodData.setTotalTradeQty( record.totalTradeQty ) ;
            eodData.setTotalTradeVal( record.totalTradeVal ) ;
            eodData.setTotalTrades( record.totalTrades ) ;

            log.debug( "->   New record added for date = " + RES_SDF.format( record.date ) );
            results.numAdditions++ ;
            histRepo.saveAndFlush( eodData ) ;
        }
        else if( histRows.size() == 1 ) {
            
            HistoricEQData histData = histRows.get( 0 ) ;
            if( histData.getPrevClose() == null || 
                histData.getPrevClose() == 0 ) {

                log.debug( "->   Existing record modified for date = " + RES_SDF.format( record.date ) );
                histData.setPrevClose( record.prevClose ) ;
                results.numModified++ ;
                histRepo.saveAndFlush( histData ) ;
            }
            else {
                log.debug( "->   Records exist for date = " + RES_SDF.format( record.date ) );
            }
        }
        else {
            deleteDuplicateHistoricData( histRows, record.prevClose, results ) ;
        }
    }
    
    private void deleteDuplicateHistoricData( List<HistoricEQData> candles,
                                              float prevClose,
                                              ImportResult results ) {
        
        if( candles != null && !candles.isEmpty() && candles.size()>1 ) {
            
            HistoricEQData histData = candles.get( 0 ) ;
            
            if( histData.getPrevClose() == null || 
                histData.getPrevClose() == 0 ) {

                log.debug( "->   Existing record modified for date = " +
                           RES_SDF.format( histData.getDate() ) );

                histData.setPrevClose( prevClose ) ;
                results.numModified++ ;
                histRepo.saveAndFlush( histData ) ;
            }

            for( int i=1; i<candles.size(); i++ ) {
                histData = candles.get( i ) ;
                log.debug( "->   Existing record deleted for date = " +
                           RES_SDF.format( histData.getDate() ) );

                results.numDeletions++ ;
                histRepo.delete( histData ) ;
            }
        }
    }
}
