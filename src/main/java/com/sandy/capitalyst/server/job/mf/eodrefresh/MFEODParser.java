package com.sandy.capitalyst.server.job.mf.eodrefresh;

import java.io.BufferedReader ;
import java.io.StringReader ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.job.equity.eodrefresh.NSEBhavcopyDownloader ;
import com.sandy.common.util.StringUtil ;

public class MFEODParser {

    private static final Logger log = Logger.getLogger( NSEBhavcopyDownloader.class ) ;
    
    private static final String BASE_URL = "https://www.amfiindia.com/spages/NAVAll.txt" ;
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
    
    private Map<String, Float> latestNAVMap = new HashMap<>() ;
    private Date navDate = null ;
    
    public void downloadMFEODReport() throws Exception {
        
        log.debug( "Downloading MF EOD report..." ) ;
        
        String reportContents = downloader.getResource( BASE_URL ) ;
        parseReportContents( reportContents ) ;
    }
    
    public Date getLatestNAVDate() {
        return this.navDate ;
    }
    
    public Float getLatestNav( String isin ) {
        return latestNAVMap.get( isin ) ;
    }
    
    private void parseReportContents( String reportContents ) 
        throws Exception {
        
        latestNAVMap.clear() ;
        navDate = null ;
        
        BufferedReader reader = new BufferedReader( new StringReader( reportContents ) ) ;
        String line = reader.readLine() ;
        boolean headerLine = true ;
        
        while( line != null ) {
            if( headerLine ) {
                headerLine = false ;
            }
            else if( !StringUtil.isEmptyOrNull( line ) ) {
                if( line.contains( ";" ) ) {
                    String[] parts = line.split( ";" ) ;
                    if( parts.length != 6 ) {
                        log.debug( line ) ;
                        throw new Exception( "Non standard line found." ) ;
                    }
                    else {
                        populateMap( parts ) ;
                    }
                }
            }
            line = reader.readLine() ;
        }
    }
    
    private void populateMap( String[] parts ) 
        throws Exception {
        
        String isinGrowth   = parts[1] ;
        String isinReinvest = parts[2] ;
        Float  latestNAV    = null ;
        
        if( !parts[4].equals( "N.A." ) ) {
            latestNAV = Float.parseFloat( parts[4] ) ;
        }
        else {
            return ;
        }
        
        if( navDate == null ) {
            navDate = SDF.parse( parts[5] ) ;
            log.debug( "Latest MF Nav date = " + navDate ) ;
        }
        
        if( !isinGrowth.equals( "-" ) ) {
            latestNAVMap.put( isinGrowth, latestNAV ) ;
        }
        
        if( !isinReinvest.equals( "-" ) ) {
            latestNAVMap.put( isinReinvest, latestNAV ) ;
        }
    }
    
    public static void main( String[] args ) throws Exception {
        MFEODParser app = new MFEODParser() ;
        app.downloadMFEODReport() ;
    }
}
