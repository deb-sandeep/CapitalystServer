package com.sandy.capitalyst.server.test.bhavcopy;

import java.math.BigInteger ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;

public class DownloadTester {
    
    private static final Logger log = Logger.getLogger( DownloadTester.class ) ;
    
    private HTTPResourceDownloader downloader = null ;
    
    public DownloadTester() {
        downloader = HTTPResourceDownloader.instance() ;
    }
    
    public void testDownload() throws Exception {
        String contents = downloader.getResource( "https://nsdl.co.in/mutual-fund-popup.html",
                                                  "nsdl-mf.txt" ) ;
        log.debug( contents ) ;
    }

    public static void main( String[] args ) throws Exception {
        log.debug( new BigInteger( "1465110000" ) );
    }
}
