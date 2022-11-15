package com.sandy.capitalyst.server.daemon.equity.intraday;

import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;

public class EQIntradayImporter {

    private static final Logger log = Logger.getLogger( EQIntradayImporter.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;
    
    private static final String EQ_ITD_URL = 
        "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY+TOTAL+MARKET" ;
    
    private static final String ETF_ITD_URL = 
        "https://www.nseindia.com/api/etf" ;
    
    private static final String COOKIE_FETCH_URL = 
        "https://www.nseindia.com/market-data/exchange-traded-funds-etf" ;
    
    @FunctionalInterface
    private static interface ITDSnapshotBuilder {
        ITDSnapshot buildSnapshot( JsonNode node ) ;
    }
    
    public EQIntradayImporter() {}
    
    private List<ITDSnapshot> getITDSnapshots() {
        
        List<ITDSnapshot> snapshots = new ArrayList<>() ;
        
        log.debug( "- Collating Equity ITD snapshots" ) ;
        collateEquityITDSnapshot( snapshots, EQ_ITD_URL,  
                                  this::buildEquityITDSnapshot ) ;
        
        log.debug( "- Collating ETF ITD snapshots" ) ;
        collateEquityITDSnapshot( snapshots, ETF_ITD_URL, 
                                  this::buildETFITDSnapshot ) ;
        
        return snapshots ;
    }
    
    private void collateEquityITDSnapshot( List<ITDSnapshot> snapshots,
                                           String url,
                                           ITDSnapshotBuilder builder ) {
        try {
            String       jsonStr   = downloadIntradaySnapshotJSON( url ) ;
            ObjectMapper objMapper = new ObjectMapper() ;
            JsonNode     jsonRoot  = objMapper.readTree( jsonStr ) ;
            
            Date timestamp = SDF.parse( jsonRoot.get( "timestamp" ).asText() ) ;
            
            JsonNode dataNode  = jsonRoot.get( "data" ) ;
            for( int i=1; i<dataNode.size(); i++ ) {
                
                JsonNode jsonNode = dataNode.get( i ) ;
                ITDSnapshot snapshot = builder.buildSnapshot( jsonNode ) ;
                
                if( snapshot != null ) {
                    if( snapshot.getLastUpdateTime() == null ) {
                        snapshot.setLastUpdateTime( timestamp ) ;
                    }
                    snapshots.add( snapshot ) ;
                }
            }
        }
        catch( Exception e ) {
            log.error( "Error obtaining ITD snapshot. " + e.getMessage() ) ;
        }
    }
    
    private ITDSnapshot buildEquityITDSnapshot( JsonNode node ) {
        
        if( !node.get( "series" ).asText().equals( "EQ" ) ) {
            return null ;
        }

        ITDSnapshot itd = new ITDSnapshot() ;
        
        try {
            itd.setSymbol           ( node.get( "symbol"            ).asText() ) ;
            itd.setIsin             ( node.get( "meta"              ).get( "isin" ).asText() ) ;
            itd.setOpen             ( node.get( "open"              ).asDouble() ) ;
            itd.setDayHigh          ( node.get( "dayHigh"           ).asDouble() ) ;
            itd.setDayLow           ( node.get( "dayLow"            ).asDouble() ) ;
            itd.setLastPrice        ( node.get( "lastPrice"         ).asDouble() ) ;
            itd.setChange           ( node.get( "change"            ).asDouble() ) ;
            itd.setPChange          ( node.get( "pChange"           ).asDouble() ) ;
            itd.setTotalTradedVolume( node.get( "totalTradedVolume" ).asDouble() ) ;
            itd.setTotalTradedValue ( node.get( "totalTradedValue"  ).asDouble() ) ;
            itd.setLastUpdateTime   ( SDF.parse( node.get( "lastUpdateTime" ).asText() ) ) ;
            itd.setETFSec           ( false ) ;
        }
        catch( ParseException e ) {
            itd = null ;
            log.debug( "Can't parse date." ) ;
        }
        
        return itd ;
    }

    private ITDSnapshot buildETFITDSnapshot( JsonNode node ) {
        
        ITDSnapshot itd = new ITDSnapshot() ;
        
        itd.setSymbol           ( node.get( "symbol" ).asText() ) ;
        itd.setIsin             ( node.get( "meta"   ).get( "isin" ).asText() ) ;
        itd.setOpen             ( node.get( "open"   ).asDouble() ) ;
        itd.setDayHigh          ( node.get( "high"   ).asDouble() ) ;
        itd.setDayLow           ( node.get( "low"    ).asDouble() ) ;
        itd.setLastPrice        ( node.get( "ltP"    ).asDouble() ) ;
        itd.setChange           ( node.get( "chn"    ).asDouble() ) ;
        itd.setPChange          ( node.get( "per"    ).asDouble() ) ;
        itd.setTotalTradedVolume( node.get( "qty"    ).asDouble() ) ;
        itd.setTotalTradedValue ( node.get( "trdVal" ).asDouble() ) ;
        itd.setETFSec           ( true ) ;
        
        return itd ;
    }
    
    private String downloadIntradaySnapshotJSON( String url ) throws Exception {
        
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        String response = null ;
        
        if( !downloader.hasCookie( "nseappid" ) ) {
            log.debug( "-> Obtaining cookies" ) ;
            downloader.getResource( COOKIE_FETCH_URL, "itd-pricevol.txt" ) ;
        }
        
        log.info( "-> Downloading intraday data." ) ;
        response = downloader.getResource( url, "itd-pricevol.txt" ) ;
        
        log.debug( "->> Done. Response size " + response.length() + " bytes." ) ;
        
        return response ;
    }
    
    
    public static void main( String[] args ) throws Exception {
        
        for( int i=0; i<5; i++ ) {
            List<ITDSnapshot> snapshots = new EQIntradayImporter().getITDSnapshots() ;
            log.debug( "Snapshots obtained. # = " + snapshots.size() ) ;
            Thread.sleep( 10000 ) ;
        }
    }
}
