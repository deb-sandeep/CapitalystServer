package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.ByteArrayInputStream ;
import java.io.File ;
import java.io.FileOutputStream ;
import java.util.Date ;
import java.util.zip.ZipEntry ;
import java.util.zip.ZipInputStream ;

import org.apache.log4j.Logger ;
import org.jsoup.Jsoup ;
import org.jsoup.nodes.Document ;
import org.jsoup.select.Elements ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.common.util.StringUtil ;

public class NSEBhavcopyDownloader {

    private static final Logger log = Logger.getLogger( NSEBhavcopyDownloader.class ) ;
    
    private static final String BASE_URL = "https://www1.nseindia.com/products/dynaContent/equities/equities/htms/CM_homepage_1.htm" ;
    private static final int BUFFER_SIZE = 10*1024 ;
    
    private HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
    
    private String bhavcopyUrl = null ;
    
    public Date getLatestBhavcopyDate() throws Exception {
        bhavcopyUrl = findBhavcopyDownloadLink() ;
        String subStr = bhavcopyUrl.substring( bhavcopyUrl.indexOf( "/cm" ) ) ;
        String dtStr  = subStr.substring( 3, subStr.length()-12 ) ;
        return NSEBhavcopyImportJob.SDF.parse( dtStr ) ;
    }
    
    public File downloadBhavcopy() throws Exception {
        
        log.debug( "Downloading latest bhavcopy..." ) ;
        
        if( StringUtil.isEmptyOrNull( bhavcopyUrl ) ) {
            bhavcopyUrl = findBhavcopyDownloadLink() ;
        }
        log.debug( "\tDownload url : " + bhavcopyUrl ) ;
        
        byte[] zipContents = downloader.getResourceAsBytes( bhavcopyUrl, "nse-bhav.txt" ) ;
        log.debug( "\tZip contents downloaded." ) ;
        
        File file = unzipAndStoreContents( zipContents ) ;
        log.debug( "\tBhavcopy contents unzipped" ) ;
        log.debug( "\tSaved file : " + file.getAbsolutePath() ) ;
        
        return file ;
    }
    
    private String findBhavcopyDownloadLink() 
        throws Exception {
        
        String content = downloader.getResource( BASE_URL, "nse-bhav.txt" ) ;
        Document doc = Jsoup.parse( content ) ;
        Elements elements = doc.select( "a:containsOwn(Bhavcopy file (csv))" ) ;
        
        if( elements.isEmpty() ) {
            throw new Exception( "Bhavcopy download link not found." ) ;
        }
        
        String href = elements.get( 0 ).attr( "href" ) ;
        return "https://www1.nseindia.com" + href ;
    }
    
    private File unzipAndStoreContents( byte[] zipContents ) 
        throws Exception {
        
        ZipInputStream zipIn = new ZipInputStream( new ByteArrayInputStream( zipContents ) ) ;
        ZipEntry entry = zipIn.getNextEntry() ;
        
        File destFile = getDestinationFile( entry.getName() ) ;
        FileOutputStream fOs = new FileOutputStream( destFile ) ;
        
        byte[] bytesIn = new byte[BUFFER_SIZE] ;
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            fOs.write(bytesIn, 0, read);
        }
        fOs.flush() ;
        fOs.close() ;
        zipIn.closeEntry();
        zipIn.close();
        
        return destFile ;
    }
    
    private File getDestinationFile( String name ) {
        
        CapitalystConfig cfg = CapitalystServer.getConfig() ;
        File baseDir = null ;
        if( cfg == null ) {
            baseDir = new File( "/home/sandeep/temp/bhavcopies/" ) ;
        }
        else {
            baseDir = new File( cfg.getWorkspaceDir(), "bhavcopies" ) ;
        }
        baseDir.mkdirs() ;
        return new File( baseDir, name ) ;
    }
}
