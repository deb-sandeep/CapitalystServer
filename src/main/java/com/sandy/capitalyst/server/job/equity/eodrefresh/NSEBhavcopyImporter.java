package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.File ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.common.util.StringUtil ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class NSEBhavcopyImporter {
    
    static final Logger log = Logger.getLogger( NSEBhavcopyImporter.class ) ;
    
    private HistoricEQDataRepo ecRepo = null ;
    private EquityMasterRepo emRepo = null ;
    private EquityHoldingRepo ehRepo = null ;
    
    public NSEBhavcopyImporter() {
        ecRepo = CapitalystServer.getBean( HistoricEQDataRepo.class ) ;
        emRepo = CapitalystServer.getBean( EquityMasterRepo.class ) ;
        ehRepo = CapitalystServer.getBean( EquityHoldingRepo.class ) ;
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
        importBhavcopyFile( bhavcopyFile, latestAvailableBhavcopyDate ) ;
        
        return latestAvailableBhavcopyDate ;
    }
    
    private void importBhavcopyFile( File bhavcopyFile, Date date ) {
        
        Map<String, List<EquityHolding>> holdingsMap = loadHoldingsMap() ;
        
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
                    
                    if( StringUtil.isNotEmptyOrNull( em.getIndustry() ) || 
                        em.isEtf() || 
                        holdingsMap.containsKey( symbol ) ) {
                        
                        ecRepo.save( candle ) ;
                        updateEquityISINMapping( symbol, isin ) ;
                    }
                    
                    if( holdingsMap.containsKey( symbol ) ) {
                        for( EquityHolding holding : holdingsMap.get( symbol ) ) {
                            holding.setCurrentMktPrice( candle.getClose() ) ;
                            holding.setLastUpdate( date ) ;
                            ehRepo.save( holding ) ;
                        }
                    }
                }
            }
        }
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
        
        HistoricEQData candle = new HistoricEQData() ;
        candle.setSymbol( record[0] ) ;
        candle.setOpen( Float.parseFloat( record[2] ) ) ;
        candle.setHigh( Float.parseFloat( record[3] ) ) ;
        candle.setLow( Float.parseFloat( record[4] ) ) ;
        candle.setClose( Float.parseFloat( record[5] ) ) ;
        candle.setTotalTradeQty( Long.parseLong( record[8] ) ) ;
        candle.setTotalTradeVal( Float.parseFloat( record[9] ) ) ;
        candle.setTotalTrades( Long.parseLong( record[11] ) ) ;
        candle.setDate( date ) ;
        return candle ;
    }
}
