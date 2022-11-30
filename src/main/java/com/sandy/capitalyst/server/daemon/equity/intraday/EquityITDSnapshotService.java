package com.sandy.capitalyst.server.daemon.equity.intraday;

import static com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDImporterDaemon.CFG_GRP_NAME ;
import static com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDImporterDaemon.CFG_PRINT_DEBUG_STMT ;

import java.io.File ;
import java.io.FileReader ;
import java.net.SocketTimeoutException ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Properties ;
import java.util.Set ;

import javax.annotation.PostConstruct ;

import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo ;

import lombok.Data ;

@Component
public class EquityITDSnapshotService {

    private static final Logger log = Logger.getLogger( EquityITDSnapshotService.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;
    
    private static final String EQ_ITD_URL = 
        "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY+TOTAL+MARKET" ;
    
    private static final String ETF_ITD_URL = 
        "https://www.nseindia.com/api/etf" ;
    
    @Data
    public static class ITDSnapshot {

        private String symbol   = null ; 
        private Date   time     = null ; 
        private float  price    = 0    ; 
        private float  changeAmt= 0    ; 
        private float  pChange  = 0    ; 
        private long   totalVol = 0    ; 
        private float  totalVal = 0    ; 
    }
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private IndexEquityRepo ieRepo = null ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private EquityLTPRepository ltpRepo = null ;
    
    @FunctionalInterface
    private static interface ITDSnapshotBuilder {
        ITDSnapshot buildSnapshot( JsonNode node ) ;
    }
    
    private Set<String> qualifiedStocks = new HashSet<>() ;
    
    private boolean debugEnable  = true ;
    
    private File cookieFile = null ;

    public EquityITDSnapshotService() {}
    
    @PostConstruct
    public void init() {
        
        log.debug( "Initializing Equity ITD snapshot service." ) ;
        
        cookieFile = new File( CapitalystServer.getConfig().getWorkspaceDir(),
                               "cookies/nse-itd-cookie.properties" ) ;
        
        ehRepo.findNonZeroHoldings()
              .forEach( h -> qualifiedStocks.add( h.getSymbolNse() ) ) ;
        
        ieRepo.findEquitiesForIndex( "Nifty 200" )
              .forEach( qualifiedStocks::add ) ;
        
        emRepo.findETFStocks()
              .forEach( qualifiedStocks::add ) ;
        
        log.debug( "-> # qualified stocks = " + qualifiedStocks.size() ) ;
    }
    
    public List<ITDSnapshot> getSnapshots() {
        
        refreshConfiguration() ;
        
        List<ITDSnapshot> snapshots = new ArrayList<>() ;
        
        try {
            if( debugEnable ) {
                log.debug( "- Collating Equity ITD snapshots" ) ;
            }
            collateEquityITDSnapshot( snapshots, EQ_ITD_URL, 
                                      "itd-eq-pricevol.txt", 
                                      this::buildEquityITDSnapshot, 2 ) ;
            
            if( debugEnable ) {
                log.debug( "- Collating ETF ITD snapshots" ) ;
            }
            collateEquityITDSnapshot( snapshots, ETF_ITD_URL, 
                                      "itd-etf-pricevol.txt",
                                      this::buildETFITDSnapshot, 2 ) ;
        }
        catch( SocketTimeoutException e ) {
            // Do nothing.
            // This catch is to enable escape from any of the network calls
            // which has not succeeded after retries.
        }
        
        return snapshots ;
    }
    
    private void collateEquityITDSnapshot( List<ITDSnapshot> snapshots,
                                           String url,
                                           String headerFile,
                                           ITDSnapshotBuilder builder,
                                           int remainingTries ) 
        throws SocketTimeoutException {
        
        try {
            String       jsonStr   = downloadIntradaySnapshotJSON( url, headerFile ) ;
            ObjectMapper objMapper = new ObjectMapper() ;
            JsonNode     jsonRoot  = objMapper.readTree( jsonStr ) ;
            
            Date timestamp = SDF.parse( jsonRoot.get( "timestamp" ).asText() ) ;
            if( debugEnable ) {
                log.debug( "-> Snapshot timestamp " + SDF.format( timestamp ) ) ;
            }
            
            JsonNode dataNode  = jsonRoot.get( "data" ) ;
            for( int i=1; i<dataNode.size(); i++ ) {
                
                JsonNode jsonNode = dataNode.get( i ) ;
                ITDSnapshot snapshot = builder.buildSnapshot( jsonNode ) ;
                
                if( snapshot != null ) {
                    if( qualifiedStocks.contains( snapshot.symbol ) ) {
                        if( snapshot.getTime() == null ) {
                            snapshot.setTime( timestamp ) ;
                        }
                        snapshots.add( snapshot ) ;
                        ltpRepo.addSnapshot( snapshot ) ;
                    }
                }
            }
        }
        catch( SocketTimeoutException ste ) {
            
            log.debug( "->  Socket timeout." ) ;
            
            if( remainingTries > 0 ) {
                if( debugEnable ) {
                    log.debug( "->  Timeout detected. Retrying" ) ;
                }
                try {
                    Thread.sleep( 2000 ) ;
                }
                catch( InterruptedException e ) {}
                collateEquityITDSnapshot( snapshots, url, headerFile,
                                          builder, remainingTries-1 );
            }
            else {
                if( debugEnable ) {
                    log.debug( "->> Timeout detected. Max retries quenched." ) ;
                    throw new SocketTimeoutException( "Socket timeout after retries." ) ;
                }
            }
        }
        catch( Exception e ) {
            log.error( "->  Error obtaining ITD snapshot. " + e.getMessage() ) ;
        }
    }
    
    private ITDSnapshot buildEquityITDSnapshot( JsonNode node ) {
        
        if( !node.get( "series" ).asText().equals( "EQ" ) ) {
            return null ;
        }

        ITDSnapshot itd = new ITDSnapshot() ;
        
        try {
            itd.setSymbol   ( node.get( "symbol"                   ).asText() ) ;
            itd.setPrice    ( (float)node.get( "lastPrice"         ).asDouble() ) ;
            itd.setChangeAmt( (float)node.get( "change"            ).asDouble() ) ;
            itd.setPChange  ( (float)node.get( "pChange"           ).asDouble() ) ;
            itd.setTotalVol ( (long )node.get( "totalTradedVolume" ).asDouble() ) ;
            itd.setTotalVal ( (float)node.get( "totalTradedValue"  ).asDouble() ) ;
            itd.setTime     ( SDF.parse( node.get( "lastUpdateTime" ).asText() )) ;
        }
        catch( ParseException e ) {
            itd = null ;
            log.debug( "Can't parse date." ) ;
        }
        
        return itd ;
    }

    private ITDSnapshot buildETFITDSnapshot( JsonNode node ) {
        
        ITDSnapshot itd = new ITDSnapshot() ;
        
        itd.setSymbol   ( node.get( "symbol"        ).asText() ) ;
        itd.setPrice    ( (float)node.get( "ltP"    ).asDouble() ) ;
        itd.setChangeAmt( (float)node.get( "chn"    ).asDouble() ) ;
        itd.setPChange  ( (float)node.get( "per"    ).asDouble() ) ;
        itd.setTotalVol ( (long )node.get( "qty"    ).asDouble() ) ;
        itd.setTotalVal ( (float)node.get( "trdVal" ).asDouble() ) ;
        
        return itd ;
    }
    
    private String downloadIntradaySnapshotJSON( String url,
                                                 String headerFile ) 
        throws Exception {
        
        if( debugEnable ) {
            log.info( "-> Downloading intraday data." ) ;
        }
        
        String response = null ;
        HTTPResourceDownloader httpClient = HTTPResourceDownloader.instance() ;
        Map<String, String> cookies = loadCookies() ;
        
        response = httpClient.getResource( url, headerFile, cookies ) ;
        
        if( debugEnable ) {
            log.debug( "->> Response " + response.length() + " bytes." ) ;
        }
        
        return response ;
    }
    
    private Map<String, String> loadCookies() {
        
        Map<String, String> cookies = new HashMap<>() ;
        try {
            if( cookieFile.exists() ) {
                Properties props = new Properties() ;
                props.load( new FileReader( cookieFile ) ) ;
                props.forEach( (key,value)-> {
                    cookies.put( key.toString(), value.toString() ) ;
                }) ;
            }
        }
        catch( Exception e ) {
            log.error( "Error loading nse itd cookies", e ) ;
        }
        return cookies ;
    }
    
    private void refreshConfiguration() {
        
        NVPManager nvpMgr = NVPManager.instance() ;
        NVPConfigGroup cfg = nvpMgr.getConfigGroup( CFG_GRP_NAME ) ; ;

        debugEnable  = cfg.getBoolValue( CFG_PRINT_DEBUG_STMT, debugEnable ) ;
    }
}