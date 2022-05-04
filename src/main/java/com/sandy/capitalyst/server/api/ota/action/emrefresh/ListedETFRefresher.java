package com.sandy.capitalyst.server.api.ota.action.emrefresh;

import java.io.File ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.common.xlsutil.XLSRow ;
import com.sandy.common.xlsutil.XLSWrapper ;

public class ListedETFRefresher extends ListedEquitiesRefresher {
    
    static final Logger log = Logger.getLogger( ListedETFRefresher.class ) ;

    private static final String LISTED_ETF_URL = 
            "https://www1.nseindia.com/content/equities/eq_etfseclist.csv" ;
    
    private Map<String, EquityMaster> dbCache = new HashMap<>() ;
    
    public ListedETFRefresher( EquityMasterRefreshOTA ota ) {
        super( ota ) ;
    }

    public void refreshEquityMaster() throws Exception {
        
        ota.addResult( " Downloading from network." ) ;
        List<XLSRow> rows = parseOnlineXLSX() ;
        ota.addResult( " Network data downloaded." ) ;
        
        ota.addResult( " Loading database cache" ) ; 
        loadDBCache() ;
        ota.addResult( " DB Cache loaded." ) ;

        ota.addResult( " Refreshing ETF symbols" ) ;
        for( XLSRow row : rows ) {
            
            String symbol       = row.getCellValue( 0 ) ;
            String securityName = row.getCellValue( 2 ) ;
            String isin         = row.getCellValue( 5 ) ;
            
            super.refresh( symbol, securityName, isin, true ) ;
        }
        ota.addResult( " ETF symbols refreshed" ) ;
        
        if( !dbCache.isEmpty() ) {
            ota.addResult( " Purging delisted ETFs" ) ;
            removeDelistedSymbols( false ) ;
            ota.addResult( " Delisted ETFs purged" ) ;
        }
    }
    
    // Remove all non ETF symbols
    protected void loadDBCache() throws Exception {
        
        super.loadDBCache() ;
        
        Iterator<Entry<String, EquityMaster>> iter = null ;
        
        iter = dbCache.entrySet().iterator() ;
        while( iter.hasNext() ) {
            EquityMaster master = iter.next().getValue() ;
            if( !master.isEtf() ) {
                iter.remove() ;
            }
        }
    }
    
    private List<XLSRow> parseOnlineXLSX() throws Exception {

        byte[] contents = downloader.getResourceAsBytes( LISTED_ETF_URL ) ;
        
        File tmpFile = File.createTempFile( "listed-etf", ".xlsx" ) ;
        FileUtils.writeByteArrayToFile( tmpFile, contents ) ;
        
        XLSWrapper xlsWrapper = new XLSWrapper( tmpFile ) ;
        return xlsWrapper.getRows( 0, 0, 6 ) ;
    }
}
