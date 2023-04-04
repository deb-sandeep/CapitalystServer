package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.ByteArrayInputStream ;
import java.io.File ;
import java.io.FileOutputStream ;
import java.util.zip.ZipEntry ;
import java.util.zip.ZipInputStream ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo.ReportMeta;

public class NSEBhavcopyDownloader {

    private static final Logger log = Logger.getLogger( NSEBhavcopyDownloader.class ) ;
    
    private static final int BUFFER_SIZE = 10*1024 ;
    
    public File downloadBhavcopy( ReportMeta reportMeta ) throws Exception {
        
        log.debug( "Downloading bhavcopy for " + reportMeta.getTradingDate() ) ;

        File savedFile = null ;
        byte[] zipContents = null ;
        String bhavcopyUrl = reportMeta.getReportURL() ;
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;

        log.debug( "\tDownload url : " + bhavcopyUrl ) ;

        zipContents = downloader.getResourceAsBytes( bhavcopyUrl,
                                  "nse-reports-headers.txt" ) ;
        savedFile = unzipAndStoreContents( zipContents ) ;

        log.debug( "\tZip contents downloaded." ) ;

        log.debug( "\tBhavcopy contents unzipped" ) ;
        log.debug( "\tSaved file : " + savedFile.getAbsolutePath() ) ;
        
        return savedFile ;
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
