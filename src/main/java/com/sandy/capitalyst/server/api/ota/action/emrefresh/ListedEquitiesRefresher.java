package com.sandy.capitalyst.server.api.ota.action.emrefresh;

import java.io.StringReader ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class ListedEquitiesRefresher {
    
    private static final Logger log = Logger.getLogger( ListedEquitiesRefresher.class ) ;

    private static final String LISTED_EQUITIES_URL = 
            "https://www1.nseindia.com/content/equities/EQUITY_L.csv" ;
    
    private EquityMasterRepo  emRepo = null ;
    private EquityHoldingRepo ehRepo = null ;
    
    protected HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
    
    protected EquityMasterRefreshOTA ota = null ;
    protected Map<String, EquityMaster> dbCache = new HashMap<>() ;
    
    public ListedEquitiesRefresher( EquityMasterRefreshOTA ota ) {
        
        this.ota = ota ;
        
        this.emRepo = CapitalystServer.getBean( EquityMasterRepo.class ) ;
        this.ehRepo = CapitalystServer.getBean( EquityHoldingRepo.class ) ;
    }

    public void refreshEquityMaster() throws Exception {
        
        ota.addResult( " Downloading from network." ) ;
        List<String[]> csvRows = parseOnlineCSV() ;
        ota.addResult( " Network data downloaded." ) ;
        
        ota.addResult( " Loading database cache" ) ; 
        loadDBCache() ;
        ota.addResult( " DB Cache loaded." ) ;
        
        ota.addResult( " Refreshing equity master." ) ;
        refreshEquityMaster( csvRows ) ;
        
        int numSymbolsToPurge = getNumNonETFDelistedEquities() ;
        if( numSymbolsToPurge > 0 ) {
            ota.addResult( " Removing " + numSymbolsToPurge + " delisted symbols." ) ;
            removeDelistedSymbols( true ) ;
            ota.addResult( " Delisted symbols purged." ) ;
        }
    }
    
    protected void loadDBCache() throws Exception {
        
        dbCache.clear() ;
        
        Iterator<EquityMaster> iter = emRepo.findAll().iterator() ;
        while( iter.hasNext() ) {
            EquityMaster master = iter.next() ;
            dbCache.put( master.getSymbol(), master ) ;
        }
    }
    
    private int getNumNonETFDelistedEquities() {
        int count = 0 ;
        for( EquityMaster em : dbCache.values() ) {
            if( !em.isEtf() ) {
                count++ ;
            }
        }
        return count ;
    }

    public List<String[]> parseOnlineCSV() throws Exception {
        
        String            resource  = null ;
        CsvParserSettings settings  = null ;
        CsvParser         csvParser = null ;
        List<String[]>    csvData   = null ;
        
        resource = downloader.getResource( LISTED_EQUITIES_URL ) ;
        
        settings = new CsvParserSettings() ;
        settings.detectFormatAutomatically() ;
        
        csvParser = new CsvParser( settings ) ;
        csvData = csvParser.parseAll( new StringReader( resource ) ) ;
        
        return csvData ;
    }
    
    private void refreshEquityMaster( List<String[]> csvRows )
        throws Exception {
        
        for( int i=0; i<csvRows.size(); i++ ) {
            String[] row = csvRows.get( i ) ;
            
            String symbol = row[0].trim() ;
            String name   = row[1].trim() ;
            String series = row[2].trim() ;
            String isin   = row[6].trim() ;
            
            if( series.equals( "EQ" ) ) {
                refresh( symbol, name, isin, false ) ;
            }
            
            if( i % 25 == 0 ) {
                ota.addResult( "   " + i + " of " + csvRows.size() + " completed."  ) ;
            }
        }
    }
    
    protected void refresh( String symbol, String name, 
                            String isin, boolean isEtf ) {
        
        log.debug( " Refreshing " + symbol + " - " + name ) ;
        
        EquityMaster eqMaster = emRepo.findBySymbol( symbol ) ;
        if( eqMaster != null ) {
            if( StringUtil.isEmptyOrNull( eqMaster.getName() ) ||
                !eqMaster.getName().equals( name ) ) {
                
                eqMaster.setName( name ) ;
                emRepo.save( eqMaster ) ;
            }
            dbCache.remove( symbol ) ;
        }
        else {
            // If a record for a symbol is not found, check by ISIN
            eqMaster = emRepo.findByIsin( isin ) ;
            if( eqMaster != null ) {
                
                // This means that the symbol has changed
                eqMaster.setSymbol( symbol ) ;
                
                // Update the equity holding tables
                ehRepo.updateSymbolNSE( isin, symbol ) ;
                
                emRepo.save( eqMaster ) ;
                dbCache.remove( symbol ) ;
            }
            else {
                // If a record is neither found by symbol or isin, create 
                // a new record
                eqMaster = new EquityMaster() ;
                eqMaster.setIsin( isin ) ;
                eqMaster.setName( name ) ;
                eqMaster.setSymbol( symbol ) ;
                eqMaster.setEtf( isEtf ) ;
                
                emRepo.save( eqMaster ) ;
            }
        }
    }
    
    protected void removeDelistedSymbols( boolean excludeEtf ) {
        
        Iterator<Entry<String, EquityMaster>> iter = null ;
        
        iter = dbCache.entrySet().iterator() ;
        while( iter.hasNext() ) {
            
            Entry<String, EquityMaster> entry = iter.next() ;
            
            String       symbol = entry.getKey() ;
            EquityMaster em     = entry.getValue() ;
            
            if( excludeEtf && em.isEtf() ) {
                continue ;
            }
            
            // We need to remove the equity master, but first we check if
            // this symbol is being used in equity holding. If so, we have
            // an issue. We need to notify user to manually update
            ota.addResult( " Removing symbol " + symbol ) ;
            
            if( !ehRepo.findBySymbolNse( symbol ).isEmpty() ) {
                ota.addResult( " ALERT: " + symbol + " unlisted but in holding." ) ;
            }
            emRepo.delete( em ) ;
        }
    }
}
