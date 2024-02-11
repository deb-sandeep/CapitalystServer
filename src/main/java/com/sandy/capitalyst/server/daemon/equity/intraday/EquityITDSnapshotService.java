package com.sandy.capitalyst.server.daemon.equity.intraday;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.capitalyst.server.CapitalystServer;
import com.sandy.capitalyst.server.core.CapitalystConfig;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager;
import com.sandy.capitalyst.server.core.util.CookieUtil;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDImporterDaemon.*;

@Component
public class EquityITDSnapshotService {

    private static final Logger log = Logger.getLogger( EquityITDSnapshotService.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;
    private static final SimpleDateFormat SNAPSHOT_SDF = new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss" ) ;
    
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
    
    private EquityHoldingRepo ehRepo = null ;
    private IndexEquityRepo ieRepo = null ;
    private EquityMasterRepo emRepo = null ;
    private EquityLTPRepository ltpRepo = null ;

    @FunctionalInterface
    private interface ITDSnapshotBuilder {
        ITDSnapshot buildSnapshot( JsonNode node ) ;
    }

    @Autowired
    public void setEhRepo(EquityHoldingRepo ehRepo) {
        this.ehRepo = ehRepo;
    }

    @Autowired
    public void setIeRepo(IndexEquityRepo ieRepo) {
        this.ieRepo = ieRepo;
    }

    @Autowired
    public void setEmRepo(EquityMasterRepo emRepo) {
        this.emRepo = emRepo;
    }

    @Autowired
    public void setLtpRepo(EquityLTPRepository ltpRepo) {
        this.ltpRepo = ltpRepo;
    }

    private final Set<String> qualifiedStocks = new HashSet<>() ;
    
    private boolean debugEnable  = true ;
    private boolean captureRawSnapshot = false ;
    
    public EquityITDSnapshotService() {}
    
    public void init() {
        
        log.debug( "Initializing Equity ITD snapshot service." ) ;
        
        ehRepo.findNonZeroHoldings()
              .forEach( h -> qualifiedStocks.add( h.getSymbolNse() ) ) ;

        qualifiedStocks.addAll( ieRepo.findEquitiesForIndex("Nifty 200") ) ;
        qualifiedStocks.addAll( emRepo.findETFStocks() ) ;
        
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
                                      this::buildEquityITDSnapshot, 2, "eqt" ) ;

            if( debugEnable ) {
                log.debug( "- Collating ETF ITD snapshots" ) ;
            }
            collateEquityITDSnapshot( snapshots, ETF_ITD_URL, 
                                      "itd-etf-pricevol.txt",
                                      this::buildETFITDSnapshot, 2, "etf" ) ;
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
                                           int remainingTries,
                                           String filePrefix ) 
        throws SocketTimeoutException {
        
        try {
            String       jsonStr   = downloadIntradaySnapshotJSON( url, headerFile, filePrefix ) ;
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
                catch( InterruptedException ignored) {}
                collateEquityITDSnapshot( snapshots, url, headerFile,
                                          builder, remainingTries-1, filePrefix );
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
            log.error( e ) ;
        }
    }
    
    private ITDSnapshot buildEquityITDSnapshot( JsonNode node ) {
        
        if( node.has( "series" ) && !node.get( "series" ).asText().equals( "EQ" ) ) {
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
                                                 String headerFile,
                                                 String filePrefix ) 
        throws Exception {
        
        if( debugEnable ) {
            log.debug( "-> Downloading intraday data." ) ;
        }
        
        String response ;
        HTTPResourceDownloader httpClient = HTTPResourceDownloader.instance() ;
        Map<String, String> cookies = CookieUtil.loadNSECookies() ;
        
        response = httpClient.getResource( url, headerFile, cookies ) ;
        
        if( debugEnable ) {
            log.debug( "->> Response " + response.length() + " bytes." ) ;
        }

        if( captureRawSnapshot ) {
            log.debug( "->> Capturing raw snapshot." ) ;
            captureRawSnapshot( filePrefix, response ) ;
        }
        return response ;
    }
    
    private void captureRawSnapshot( String filePrefix, String response )
        throws Exception {
        
        String fileName = filePrefix + "-" + SNAPSHOT_SDF.format( new Date() ) + ".json" ;
        CapitalystConfig cfg = CapitalystServer.getConfig() ;
        File snapshotDir = new File( cfg.getWorkspaceDir(), "raw_itd_snapshots" ) ;
        File snapshotFile = new File( snapshotDir, fileName ) ;
        
        FileUtils.write( snapshotFile, response, "UTF-8" ) ;
    }
    
    private void refreshConfiguration() {
        
        NVPManager nvpMgr = NVPManager.instance() ;
        NVPConfigGroup cfg = nvpMgr.getConfigGroup( CFG_GRP_NAME ) ;

        debugEnable        = cfg.getBoolValue( CFG_PRINT_DEBUG_STMT, debugEnable ) ;
        captureRawSnapshot = cfg.getBoolValue( CFG_CAPTRE_RAW_SNAPSHOT,captureRawSnapshot ) ;
    }
}
