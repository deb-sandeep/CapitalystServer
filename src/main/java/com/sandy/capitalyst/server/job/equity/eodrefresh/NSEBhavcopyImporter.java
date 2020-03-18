package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.File ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.equity.EquityCandle ;
import com.sandy.capitalyst.server.dao.equity.EquityCandleRepo ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.EquityISIN ;
import com.sandy.capitalyst.server.dao.equity.EquityISINRepo ;
import com.sandy.common.util.StringUtil ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class NSEBhavcopyImporter {
    
    static final Logger log = Logger.getLogger( NSEBhavcopyImporter.class ) ;
    
    private EquityCandleRepo ecRepo = null ;
    private EquityISINRepo eiRepo = null ;
    private EquityHoldingRepo ehRepo = null ;
    
    public NSEBhavcopyImporter() {
        ecRepo = CapitalystServer.getBean( EquityCandleRepo.class ) ;
        eiRepo = CapitalystServer.getBean( EquityISINRepo.class ) ;
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
            String isin = record[12] ;
            
            long totalTradeQty = Long.parseLong( record[8] ) ;
            
            if( series.equals( "EQ" ) && totalTradeQty > 50000 ) {
                EquityCandle candle = transformRecord( record, date ) ;
                ecRepo.save( candle ) ;
                
                if( holdingsMap.containsKey( symbol ) ) {
                    for( EquityHolding holding : holdingsMap.get( symbol ) ) {
                        holding.setCurrentMktPrice( candle.getClose() ) ;
                        ehRepo.save( holding ) ;
                    }
                }
                
                EquityISIN eqIsin = eiRepo.findByIsin( isin ) ;
                if( eqIsin == null ) {
                    eqIsin = new EquityISIN() ;
                    eqIsin.setSymbol( symbol ) ;
                    eqIsin.setIsin( isin ) ;
                    eiRepo.save( eqIsin ) ;
                }
            }
        }
    }

    private EquityCandle transformRecord( String[] record, Date date ) {
        EquityCandle candle = new EquityCandle() ;
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
