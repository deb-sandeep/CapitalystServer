package com.sandy.capitalyst.server.api.index.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringReader ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.ota.action.OTALogger ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.index.IndexEquity ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexMasterRepo ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class IndexRefresher {
    
    private static final Logger log = Logger.getLogger( IndexRefresher.class ) ;
    
    private IndexMasterRepo  imRepo = null ;
    private EquityMasterRepo emRepo = null ;
    private IndexEquityRepo  ieRepo = null ;
    
    private OTALogger otaLogger = null ;

    protected HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
    
    public IndexRefresher( OTALogger otaLogger ) {
        
        this.imRepo = getBean( IndexMasterRepo.class ) ;
        this.emRepo = getBean( EquityMasterRepo.class ) ;
        this.ieRepo = getBean( IndexEquityRepo.class ) ;
        
        this.otaLogger = otaLogger ;
    }
    
    protected void log( String msg ) {
        log.debug( "  " + msg ) ;
        if( this.otaLogger != null ) {
            this.otaLogger.addResult( "  " + msg ) ;
        }
    }
    
    protected void log( Exception e ) {
        log.debug( "  Exception: " + e.getMessage(), e ) ;
        if( this.otaLogger != null ) {
            this.otaLogger.addResult( e ) ;
        }
    }
    
    public int refreshIndex( Integer idxId ) 
        throws Exception {
        
        IndexMaster idx = imRepo.findById( idxId ).get() ;
        return refreshIndex( idx ) ;
    }

    public int refreshIndex( IndexMaster idx ) 
        throws Exception {
        
        log( "Refreshing index " + idx.getName() ) ;
        
        int numUpdates = 0 ;
        List<String[]> csvRows = parseOnlineCSV( idx.getIncludedStocksUrl() ) ;
        
        //ListOfStringPrinter.printLoS( csvRows ) ;
        
        if( csvRows != null && !csvRows.isEmpty() ) {
            
            ieRepo.deleteByIdxMaster( idx ) ;
            
            for( String[] row : csvRows ) {
                
                String industry = row[1] ;
                String symbol   = row[2] ;
                String series   = row[3] ;
                
                if( series.equals( "EQ" ) ) {
                    insertMapping( idx, symbol, industry ) ;
                    log( "    " + symbol ) ;
                    numUpdates++ ;
                }
            }
        }
        return numUpdates ;
    }
    
    private void insertMapping( IndexMaster idx, String symbol, String industry ) {
        
        EquityMaster em = emRepo.findBySymbol( symbol ) ;
        if( em == null ) {
            log( "INFO: Symbol " + symbol + " not found." ) ; 
        }
        else {
            IndexEquity ie = new IndexEquity() ;
            ie.setIdxMaster( idx ) ;
            ie.setEqMaster( em ) ;
            ieRepo.save( ie ) ;
            
            em.setIndustry( industry ) ;
            emRepo.save( em ) ;
        }
    }

    public List<String[]> parseOnlineCSV( String url ) throws Exception {
        
        log( "Downloading CSV - " + url.substring( url.lastIndexOf( '/' ) ) ) ;
        
        String            resource  = null ;
        CsvParserSettings settings  = null ;
        CsvParser         csvParser = null ;
        List<String[]>    csvData   = null ;
        
        resource = downloader.getResource( url ) ;
        
        settings = new CsvParserSettings() ;
        settings.detectFormatAutomatically() ;
        
        csvParser = new CsvParser( settings ) ;
        csvData = csvParser.parseAll( new StringReader( resource ) ) ;
        
        return csvData ;
    }
}
