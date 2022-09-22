package com.sandy.capitalyst.server.api.ota.action.prevcloseupdater;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.time.DateUtils ;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

public class HistPrevCloseUpdater extends OTA {
    
    public static final String NAME = "EquityTradeUpdater" ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    public HistPrevCloseUpdater() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        HistoricEQDataRepo histRepo = getBean( HistoricEQDataRepo.class ) ;
        
        Map<String, HistoricEQData> prevHistDataMap = new HashMap<>() ;
        List<HistoricEQData> eodDataList = null ;
        
        Date markerDate = DateUtils.addYears( new Date(), -3 ) ;
        
        eodDataList = histRepo.getHistoricDataClosestInFutureToDate( markerDate ) ;
        
        while( !eodDataList.isEmpty() ) {
            
            markerDate = eodDataList.get( 0 ).getDate() ;
            super.addResult( "Processing " + SDF.format( markerDate ) ) ;
            super.addResult( "  Total symbols = " + eodDataList.size() ) ;
            
            int total = eodDataList.size() ;
            int processedCount = 0 ;
            int dupsRemoved = 0 ;
            
            Map<String, HistoricEQData> dupMap = new HashMap<>() ;
            
            for( HistoricEQData candle : eodDataList ) {
                
                HistoricEQData dupGuard = dupMap.get( candle.getSymbol() ) ;
                if( dupGuard != null ) {
                    histRepo.delete( candle ) ;
                    dupsRemoved++ ;
                    continue ;
                }
                else {
                    dupMap.put( candle.getSymbol(), candle ) ;
                }
                
                if( candle.getPrevClose() == null ) {
                    
                    HistoricEQData prevCandle = null ;
                    prevCandle = prevHistDataMap.get( candle.getSymbol() ) ;
                    if( prevCandle == null ) {
                        candle.setPrevClose( candle.getClose() ) ;
                    }
                    else {
                        candle.setPrevClose( prevCandle.getClose() ) ;
                    }
                    
                    histRepo.save( candle ) ;
                    processedCount++ ;
                    
                    if( processedCount % 100 == 0 ) {
                        super.addResult( "   " + 
                                         processedCount + " processed, " + 
                                         ( total - processedCount ) + " remaining." ) ; 
                    }
                }
                
                prevHistDataMap.put( candle.getSymbol(), candle ) ;
            }
            
            super.addResult( "   Dups removed = " + dupsRemoved ) ;
            
            eodDataList = histRepo.getHistoricDataClosestInFutureToDate( markerDate ) ;
        }
    }
}
