package com.sandy.capitalyst.server.api.equity.helper.bhavcopy;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

public class NSEBhavcopyImporter extends AbstractBhavcopyImporter {
    
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private HistoricEQDataRepo     ecRepo  = null ;
    private EquityMasterRepo       emRepo  = null ;

    public NSEBhavcopyImporter() {
        super() ;
        
        ecRepo = getBean( HistoricEQDataRepo.class ) ;
        emRepo = getBean( EquityMasterRepo.class ) ;
    }

    protected Date getBhavcopyDate( String[] record ) throws Exception {
        
        String dateStr= record[10] ;
        return DATE_FMT.parse( dateStr ) ;
    }
    
    protected boolean shouldProcessRecord( String[] record ) {
        
        String series = record[1] ;
        return series.equals( "EQ" ) ;
    }
    
    protected EquityMaster getEquityMaster( String[] record ) {
        
        String symbol = record[0] ;
        EquityMaster em = emRepo.findBySymbol( symbol ) ;
        return em ;
    }
    
    protected HistoricEQData buildEquityCandle( String[] record, Date date ) {
        
        List<HistoricEQData> candles = null ;
        HistoricEQData candle = null ;
        String symbol = record[0].trim() ;
        
        candles = ecRepo.findBySymbolAndDate( symbol, date ) ;
        
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
        
        candle.setSymbol       ( symbol ) ;
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
