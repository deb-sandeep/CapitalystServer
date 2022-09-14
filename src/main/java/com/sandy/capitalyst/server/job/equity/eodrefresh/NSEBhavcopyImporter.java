package com.sandy.capitalyst.server.job.equity.eodrefresh;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.File ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo ;
import com.sandy.common.util.StringUtil ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class NSEBhavcopyImporter {
    
    private static final Logger log = Logger.getLogger( NSEBhavcopyImporter.class ) ;

    private static final String NIFTY_200_IDX_NAME = "Nifty 200" ;
    
    private HistoricEQDataRepo ecRepo = null ;
    private EquityMasterRepo emRepo = null ;
    private EquityHoldingRepo ehRepo = null ;
    private IndexEquityRepo ieRepo = null ;

    private EquityDailyGainUpdater dgUpdater = null ;
    
    private List<String> nifty200Stocks = new ArrayList<>() ;
    
    public NSEBhavcopyImporter() {
        ecRepo  = getBean( HistoricEQDataRepo.class ) ;
        emRepo  = getBean( EquityMasterRepo.class ) ;
        ehRepo  = getBean( EquityHoldingRepo.class ) ;
        ieRepo  = getBean( IndexEquityRepo.class ) ;
        
        dgUpdater = new EquityDailyGainUpdater() ;
        
        nifty200Stocks.addAll( ieRepo.findEquitiesForIndex( NIFTY_200_IDX_NAME ) ) ;
    }

    public Date importBhavcopy( Date lastImportDate ) 
            throws Exception {
        
        NSEBhavcopyDownloader downloader = new NSEBhavcopyDownloader() ;
        Date latestAvailableBhavcopyDate = downloader.getLatestBhavcopyDate() ;
        
        if( lastImportDate != null ) {
            if( !latestAvailableBhavcopyDate.after( lastImportDate ) ) {
                return lastImportDate ;
            }
        }
        
        File bhavcopyFile = downloader.downloadBhavcopy() ;
        importBhavcopyFile( bhavcopyFile, new Date() ) ;
        
        return latestAvailableBhavcopyDate ;
    }
    
    private void importBhavcopyFile( File bhavcopyFile, Date date ) 
        throws Exception {
        
        Map<String, List<EquityHolding>> holdingsMap = loadHoldingsMap() ;
        EquityTTMPerfUpdater ttmPerfUpdater = new EquityTTMPerfUpdater() ;
        
        CsvParserSettings settings = new CsvParserSettings() ;
        settings.detectFormatAutomatically() ;
        
        CsvParser csvParser = new CsvParser( settings ) ;
        
        List<String[]> csvData = csvParser.parseAll( bhavcopyFile ) ;
        
        for( int i=1;i<csvData.size(); i++ ) {
            
            String[] record = csvData.get( i ) ;
            
            String symbol = record[0] ;
            String series = record[1] ;
            String isin   = record[12] ;
            
            if( series.equals( "EQ" ) ) {
                
                EquityMaster em = emRepo.findBySymbol( symbol ) ;
                
                if( em != null ) {
                    
                    HistoricEQData candle = buildEquityCandle( record, date ) ;
                    
                    em.setClose( candle.getClose() ) ;
                    em.setPrevClose( candle.getPrevClose() ) ;
                    emRepo.save( em ) ;
                    
                    if( StringUtil.isNotEmptyOrNull( em.getIndustry() ) || 
                        em.isEtf() || 
                        holdingsMap.containsKey( symbol ) || 
                        nifty200Stocks.contains( em.getSymbol() ) ) {
                        
                        ecRepo.save( candle ) ;
                        updateEquityISINMapping( symbol, isin ) ;
                        
                        ttmPerfUpdater.addTodayEODCandle( candle ) ;
                    }
                    
                    if( holdingsMap.containsKey( symbol ) ) {
                        for( EquityHolding holding : holdingsMap.get( symbol ) ) {
                            
                            log.debug( "Updating daily gain. Holding " + holding.getId() ) ;
                            dgUpdater.updateEDG( holding, candle ) ;
                            
                            holding.setCurrentMktPrice( candle.getClose() ) ;
                            holding.setLastUpdate( date ) ;
                            ehRepo.save( holding ) ;
                        }
                    }
                }
            }
        }
        
        ttmPerfUpdater.updateTTMPerfMeasures() ;
    }
    
    private Map<String, List<EquityHolding>> loadHoldingsMap() {
        
        List<EquityHolding> holdings = ehRepo.findNonZeroHoldings() ;
        Map<String, List<EquityHolding>> holdingsMap = new HashMap<>() ;
        
        for( EquityHolding holding : holdings ) {
            String symbol = holding.getSymbolNse() ;
            if( StringUtil.isNotEmptyOrNull( symbol ) ) {
                List<EquityHolding> holdingList = holdingsMap.get( symbol ) ;
                if( holdingList == null ) {
                    holdingList = new ArrayList<>() ;
                    holdingsMap.put( symbol, holdingList ) ;
                }
                holdingList.add( holding ) ;
            }
        }
        return holdingsMap ;
    }

    private void updateEquityISINMapping( String symbol, String isin ) {
        
        try {
            EquityMaster eqIsin = emRepo.findByIsin( isin ) ;
            if( eqIsin == null ) {
                // If we can't find a mapping via ISIN, it can also mean than
                // the ISIN has changed. In this case, we see if the we can 
                // find a mapping via the symbol before trying to create a 
                // new mapping.
                //
                // Case observed - IRCON ISIN changed on 7th May 2020
                eqIsin = emRepo.findBySymbol( symbol ) ;
                if( eqIsin != null ) {
                    eqIsin.setIsin( isin ) ;
                    
                    // In case we have updated an existing mapping, we have to
                    // check if this ISIN was being used by any of our
                    // existing holdings. If so, we update those too.
                    ehRepo.updateISIN( isin, symbol ) ;
                }
                else {
                    eqIsin = new EquityMaster() ;
                    eqIsin.setSymbol( symbol ) ;
                    eqIsin.setIsin( isin ) ;
                }
                emRepo.save( eqIsin ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Could not update equity ISIN mapping.", e ) ; 
            log.error( "\tSymbol = " + symbol ) ;
            log.error( "\tISIN = " + isin ) ;
            throw e ;
        }
    }

    private HistoricEQData buildEquityCandle( String[] record, Date date ) {
        
        List<HistoricEQData> candles = null ;
        HistoricEQData candle = null ;
        
        candles = ecRepo.findBySymbolAndDate( record[0], date ) ;
        
        if( candles == null || candles.isEmpty() ) {
            candle = new HistoricEQData() ;
        }
        else {
            candle = candles.get( 0 ) ;
            for( int i=1; i<candles.size(); i++ ) {
                candle = candles.get( i ) ;
                ecRepo.delete( candle ) ;
            }
        }
        
        candle.setSymbol       ( record[0] ) ;
        candle.setOpen         ( Float.parseFloat( record[ 2] ) ) ;
        candle.setHigh         ( Float.parseFloat( record[ 3] ) ) ;
        candle.setLow          ( Float.parseFloat( record[ 4] ) ) ;
        candle.setClose        ( Float.parseFloat( record[ 5] ) ) ;
        candle.setPrevClose    ( Float.parseFloat( record[ 7] ) ) ;
        candle.setTotalTradeQty( Long.parseLong  ( record[ 8] ) ) ;
        candle.setTotalTradeVal( Float.parseFloat( record[ 9] ) ) ;
        candle.setTotalTrades  ( Long.parseLong  ( record[11] ) ) ;
        candle.setDate         ( date ) ;
        
        return candle ;
    }
}
