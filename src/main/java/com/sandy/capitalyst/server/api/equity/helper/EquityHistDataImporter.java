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
    private static class UpdateResults {
        private int numRecordsFounds = 0 ;
        private int numAdditions = 0 ;
        private int numDeletions = 0 ;
    }
    
    private String symbol = null ;
    private Date fromDate = null ;
    private Date toDate   = null ;
    
    private UpdateResults results = new UpdateResults() ;
    
    private HistoricEQDataRepo histRepo = null ;

    public EquityHistDataImporter( String symbol, Date fromDate, Date toDate ) {
        this.symbol   = symbol ;
        this.fromDate = fromDate ;
        this.toDate   = toDate ;
        
        histRepo = getBean( HistoricEQDataRepo.class ) ;
    }
    
    public UpdateResults execute() throws Exception {
        
        log.debug( "!- Filling historic data for " + symbol + " >" ) ;
        log.debug( "-> From date = " + RES_SDF.format( fromDate ) ) ;
        log.debug( "-> To date = " + RES_SDF.format( toDate ) ) ;
        
        try {
            if( fromDate365DaysBeforeToDate() ) {
                throw new IllegalArgumentException( 
                    "From date can't be more than 365 days before the end date" ) ;
            }
            
            String csvContent = getRawHistoricData() ;
            
            if( StringUtil.isNotEmptyOrNull( csvContent ) ) {
                parseAndPopulateHistoricData( csvContent ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error updating historic data.", e ) ;
            throw e ;
        }
        finally {
            log.debug( "- Completed historic data update <<" ) ;
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
        
        for( int i=1; i<records.size(); i++ ) {
            String[] row = records.get( i ) ;
            addHistoricRecord( row ) ;
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
        
        log.debug( "- Downloading eod data." ) ;
        log.debug( "-> URL " + url ) ;
        response = downloader.getResource( url, "nse-bhav.txt" ) ;
        log.debug( "-> Done. Response size " + response.length() + " bytes." ) ;
        
        int startIndex = response.indexOf( DIV_START ) ;
        if( startIndex != -1 ) {
            startIndex += DIV_START.length() ;
            int endIndex = response.indexOf( "</div>", startIndex ) ;
            csvContent = response.substring( startIndex, endIndex ) ;
            log.debug( "-> Data size = " + csvContent.length() + " bytes." ) ;
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
            eodData.setOpen( open ) ;
            eodData.setHigh( high ) ;
            eodData.setLow( low ) ;
            eodData.setClose( close ) ;
            eodData.setTotalTradeQty( totalTradeQty ) ;
            eodData.setTotalTradeVal( totalTradeVal ) ;
            eodData.setTotalTrades( totalTrades ) ;
            
            results.numAdditions++ ;
            log.debug( "->> Adding record " + RES_SDF.format( date ) ) ;
            
            histRepo.saveAndFlush( eodData ) ;
        }
        else {
            deleteDuplicateHistoricData( histRows ) ;
        }
        
        return eodData ;
    }
    
    private void deleteDuplicateHistoricData( List<HistoricEQData> candles ) {
        
        if( candles != null && !candles.isEmpty() && candles.size()>1 ) {
            for( int i=1; i<candles.size()-1; i++ ) {
                
                HistoricEQData candle = candles.get( i ) ;
                log.debug( "Deleting duplicate candle for " + 
                           candle.getSymbol() + " @ " + 
                           RES_SDF.format( candle.getDate()  ) ) ;
                
                results.numDeletions++ ;
                log.debug( "->> Removing record " + RES_SDF.format( candle.getDate() ) ) ;
                
                histRepo.delete( candle ) ;
            }
        }
    }
}
