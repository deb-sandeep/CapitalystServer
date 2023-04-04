package com.sandy.capitalyst.server.job.equity.idxrefresh;

import java.io.StringReader ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import com.sandy.capitalyst.server.core.network.HTTPException500;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPException404 ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.dao.index.HistoricIdxData ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.HistoricIdxDataRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexMasterRepo ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

public class NSEIndexEODImporter {

    private static final Logger log = Logger.getLogger( NSEIndexEODImporter.class ) ;
    
    private static final SimpleDateFormat REQ_SDF = new SimpleDateFormat( "ddMMyyyy" ) ;
    private static final SimpleDateFormat RES_SDF = new SimpleDateFormat( "dd-MM-yyyy" ) ;
    private static final String URL = "https://archives.nseindia.com/content/indices/ind_close_all_{date}.csv" ;
    
    private Date importDate = null ;
    
    private IndexMasterRepo     imRepo  = null ;
    private HistoricIdxDataRepo eodRepo = null ;
    
    public NSEIndexEODImporter( Date importDate ) {
        
        this.importDate = importDate ;

        imRepo  = getBean( IndexMasterRepo.class ) ;
        eodRepo = getBean( HistoricIdxDataRepo.class ) ;
    }

    public int importEODValues() throws Exception {
        
        int numRecordsProcessed = 0 ;
        
        try {
            List<String[]> records = downloadCSV() ;
            
            for( String[] record : records ) {
                String idxName = record[0].trim() ;
                IndexMaster im = imRepo.findByName( idxName ) ;
                
                if( im != null && im.getHistEnabled() == Boolean.TRUE ) {
                    saveIndexEoDRecord( im, record ) ;
                    numRecordsProcessed++ ;
                }
            }
        }
        catch( HTTPException404 | HTTPException500 ex ) {
            log.debug( "No file found." ) ;
        }
        
        return numRecordsProcessed ;
    }
    
    private List<String[]> downloadCSV() throws Exception {
        
        List<String[]> records = null ;

        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        
        String url        = URL.replace( "{date}", REQ_SDF.format( importDate ) ) ;
        String csvContent = downloader.getResource( url, "nse-reports-headers.txt" ) ;

        CsvParser parser = new CsvParser( new CsvParserSettings() ) ;
        
        records = parser.parseAll( new StringReader( csvContent ) ) ;
        if( records.size() > 1 ) {
            // Remove the header row.
            records.remove( 0 ) ;
        }
        
        return records ;
    }
    
    private void saveIndexEoDRecord( IndexMaster im, String[] record ) 
        throws Exception {
        
        Date date = RES_SDF.parse( record[1].trim() ) ;
        HistoricIdxData histData = eodRepo.findByIndexAndDate( im, date ) ;
        
        if( histData == null ) {
            histData = new HistoricIdxData() ;
            histData.setIndex( im ) ;
            histData.setIndexName( im.getName() ) ;
            histData.setDate( date ) ;
        }
        
        histData.setOpen  ( Double.parseDouble( sanitize( record[2].trim() ))) ;
        histData.setHigh  ( Double.parseDouble( sanitize( record[3].trim() ))) ;
        histData.setLow   ( Double.parseDouble( sanitize( record[4].trim() ))) ;
        histData.setClose ( Double.parseDouble( sanitize( record[5].trim() ))) ;
        histData.setVolume( Long.parseLong    ( sanitize( record[8].trim() ))) ;
        
        eodRepo.saveAndFlush( histData ) ;
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
