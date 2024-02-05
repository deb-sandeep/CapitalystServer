package com.sandy.capitalyst.server.job.equity.idirectmap;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.zip.ZipEntry ;
import java.util.zip.ZipInputStream ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

import lombok.Data ;

public class IDirectSecurityMapDownloader {

    private static final Logger log = Logger.getLogger( IDirectSecurityMapDownloader.class ) ;
    
    private static final String DOWNLOAD_URL = "https://directlink.icicidirect.com/NewSecurityMaster/SecurityMaster.zip" ;
    
    @Data
    public static class SecMapping {
        private String isin        = null ;
        private String symbolICICI = null ;
        private String symbolNSE   = null ;
        private float  high52w     = 0F ;
        private float  low52w      = 0F ;
    }
    
    private HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
    
    public List<SecMapping> downloadSecurityMappings() throws Exception {
        
        log.debug( "Downloading latest Security Master..." ) ;
        
        byte[] zipContents = downloader.getResourceAsBytes( DOWNLOAD_URL ) ;
        log.debug( "\tZip contents downloaded." ) ;
        
        String csvContent = unzipAndExtractContents( zipContents ) ;
        log.debug( "\tSecurity Master contents unzipped" ) ;
        
        List<SecMapping> mappings = extractSecMappings( csvContent ) ;
        return mappings ;
    }
    
    private String unzipAndExtractContents( byte[] zipContents ) 
        throws Exception {
        
        ZipInputStream zipIn = new ZipInputStream( new ByteArrayInputStream( zipContents ) ) ;
        ZipEntry entry = zipIn.getNextEntry() ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        
        while( entry != null ) {
            if( entry.getName().equals( "NSEScripMaster.txt" ) ) {
                
                byte[] bytesIn = new byte[10*1024];
                int read = 0;
                while ((read = zipIn.read(bytesIn)) != -1) {
                    bos.write( bytesIn, 0, read ) ;
                }
                zipIn.closeEntry();
                zipIn.close();
                
                return bos.toString() ;
            }
            entry = zipIn.getNextEntry() ;
        }
        return null ;
    }
    
    private List<SecMapping> extractSecMappings( String csvContent ) {
        
        List<SecMapping>  mappings = new ArrayList<>() ;
        CsvParserSettings settings = new CsvParserSettings() ;
        settings.detectFormatAutomatically() ;
        
        CsvParser csvParser = new CsvParser( settings ) ;
        StringReader reader = new StringReader( csvContent ) ;
        
        List<String[]> csvData = csvParser.parseAll( reader ) ;
        
        for( int i=1;i<csvData.size(); i++ ) {
            
            String[] record = csvData.get( i ) ;
            
            String symbolICICI = record[1] ;
            String series      = record[2] ;
            String isin        = record[10] ;
            float  high52      = Float.parseFloat( record[11] ) ;
            float  low52       = Float.parseFloat( record[12] ) ;
            String symbolNSE   = record[60] ;
            
            if( series.equals( "EQ" ) ) {
                
                SecMapping mapping = new SecMapping() ;
                mapping.setIsin( isin ) ;
                mapping.setSymbolICICI( symbolICICI ) ;
                mapping.setSymbolNSE( symbolNSE ) ;
                mapping.setHigh52w( high52 ) ;
                mapping.setLow52w( low52 ) ;
                
                mappings.add( mapping ) ;
            }
        }
        return mappings ;
    }

    public static void main( String[] args ) throws Exception {
        IDirectSecurityMapDownloader app = new IDirectSecurityMapDownloader() ;
        List<SecMapping> mappings = app.downloadSecurityMappings() ;
        for( SecMapping m : mappings ) {
            log.debug( m.isin + " :: " + m.getSymbolICICI() + " - " + m.getSymbolNSE() ) ;
        }
    }
}
