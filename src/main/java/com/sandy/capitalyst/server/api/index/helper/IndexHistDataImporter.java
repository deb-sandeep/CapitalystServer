package com.sandy.capitalyst.server.api.index.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringReader ;
import java.net.URLEncoder ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.web.multipart.MultipartFile ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.index.HistoricIdxData ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.HistoricIdxDataRepo ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

import lombok.Data ;

/**
 * For a given NSE index and a past date range, this one-time class 
 * downloads and imports the end of day data into the database.
 */
public class IndexHistDataImporter {
    
    private static final Logger log = Logger.getLogger( IndexHistDataImporter.class ) ;
    
    private static final SimpleDateFormat REQ_SDF = new SimpleDateFormat( "dd-MM-yyyy" ) ;
    private static final SimpleDateFormat RES_SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private static final String NSE_HISTORIC_DATA_URL = 
            "https://www1.nseindia.com/" + 
            "products/dynaContent/equities/indices/historicalindices.jsp?" + 
            "indexType={indexName}&" + 
            "fromDate={fromDate}&" + 
            "toDate={toDate}&" ;
    
    private static final String DIV_START = "<div id='csvContentDiv' style='display:none;'>" ;
    
    @Data
    public static class ImportResult {
        
        private String  indexName        = null ;
        private int     numRecordsFounds = 0 ;
        private int     numAdditions     = 0 ;
    }
    
    private IndexMaster index    = null ;
    private Date        fromDate = null ;
    private Date        toDate   = null ;
    
    private ImportResult results = new ImportResult() ;
    
    private HistoricIdxDataRepo histRepo = null ;

    public IndexHistDataImporter( IndexMaster index, Date fromDate, Date toDate ) {
        histRepo = getBean( HistoricIdxDataRepo.class ) ;
        this.index     = index ;
        this.fromDate  = fromDate ;
        this.toDate    = toDate ;
    }
    
    public ImportResult execute() throws Exception {
        
        log.info( "!- Filling historic data for " + index.getName() + " >" ) ;
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
            log.info( "-> Total eod records    = " + histRepo.getNumRecords( index ) ) ;
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
    
    public ImportResult importFile( MultipartFile file )
        throws Exception {
        
        String fileContent = new String( file.getBytes() ) ;
        results = parseAndPopulateHistoricData( fileContent ) ;
        return results ;
    }

    private ImportResult parseAndPopulateHistoricData( String csvContent )
            throws Exception {
        
        CsvParserSettings settings = new CsvParserSettings() ;
        CsvParser parser = new CsvParser( settings ) ;
        List<String[]> records = null ;
        
        csvContent = csvContent.replace( ":", "\n" ) ;
        records = parser.parseAll( new StringReader( csvContent ) ) ;
        
        results.setNumRecordsFounds( records.size()-1 ) ;
        log.debug( "-> Num records found = " + (records.size()-1) );
        
        log.info( "-> Importing eod data." ) ;
        if( records.size() > 1 ) {

            results.indexName = this.index.getName() ;
            
            for( int i=1; i<records.size(); i++ ) {
                String[] row = records.get( i ) ;
                addHistoricRecord( row ) ;
            }
        }
        return results ;
    }

    private boolean fromDate365DaysBeforeToDate() {
        Date maxPastDate = DateUtils.addDays( toDate, -365 ) ;
        return maxPastDate.after( fromDate ) ;
    }
    
    private String getRawHistoricData() throws Exception {
        
        String translatedIdxName = IndexHistKeyMap.translate( index.getName() ) ;
        String encodedIdx = URLEncoder.encode( translatedIdxName, "UTF-8" ) ;
        
        log.debug( "- Translated index name = " + translatedIdxName ) ;
        
        String url = NSE_HISTORIC_DATA_URL
                        .replace( "{indexName}", encodedIdx )
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

    private HistoricIdxData addHistoricRecord( String[] row ) throws Exception {
        
        HistoricIdxData histRow = null ;
        
        // For yesterday record, the total trade quantity is not obtained.
        try {
            Long.parseLong( row[5].trim() ) ;
        }
        catch( Exception e ) {
            row[5] = "0" ;
        }
        
        Date   date          = RES_SDF.parse   ( row[0].trim() ) ;
        float  open          = Float.parseFloat( sanitize( row[1].trim() )) ;
        float  high          = Float.parseFloat( sanitize( row[2].trim() )) ;
        float  low           = Float.parseFloat( sanitize( row[3].trim() )) ;
        float  close         = Float.parseFloat( sanitize( row[4].trim() )) ;
        long   totalTradeQty = Long.parseLong  ( sanitize( row[5].trim() )) ;        

        histRow = histRepo.findByIndexAndDate( index, date ) ;
        
        if( histRow == null ) {
            
            histRow = new HistoricIdxData() ;
            
            histRow.setIndex     ( index           ) ;
            histRow.setIndexName ( index.getName() ) ;
            histRow.setDate      ( date            ) ;
            histRow.setOpen      ( open            ) ;
            histRow.setHigh      ( high            ) ;
            histRow.setLow       ( low             ) ;
            histRow.setClose     ( close           ) ;
            histRow.setVolume    ( totalTradeQty   ) ;
            
            results.numAdditions++ ;
            histRepo.saveAndFlush( histRow ) ;
        }
        return histRow ;
    }

    private String sanitize( String str ) {
        if( str.equals( "-" ) ) return "0" ;
        try {
            Double.parseDouble( str ) ;
        }
        catch( Exception e ) {
            return "0" ;
        }
        return str ;
    }
}
