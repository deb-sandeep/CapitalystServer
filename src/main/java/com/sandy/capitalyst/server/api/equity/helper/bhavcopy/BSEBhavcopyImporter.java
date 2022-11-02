package com.sandy.capitalyst.server.api.equity.helper.bhavcopy;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

public class BSEBhavcopyImporter extends AbstractBhavcopyImporter {
    
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat( "dd-MMM-yy" ) ;
    
    private HistoricEQDataRepo     ecRepo  = null ;
    private EquityMasterRepo       emRepo  = null ;

    public BSEBhavcopyImporter() {
        super() ;
        
        ecRepo = getBean( HistoricEQDataRepo.class ) ;
        emRepo = getBean( EquityMasterRepo.class ) ;
    }

    protected Date getBhavcopyDate( String[] record ) throws Exception {
        
        String dateStr= record[15] ;
        return DATE_FMT.parse( dateStr ) ;
    }
    
    protected boolean shouldProcessRecord( String[] record ) {
        
        return getEquityMaster( record ) != null ;
    }
    
    protected EquityMaster getEquityMaster( String[] record ) {
        
        String isin = record[14] ;
        EquityMaster em = emRepo.findByIsin( isin ) ;
        return em ;
    }
    
    protected HistoricEQData buildEquityCandle( String[] record, Date date ) {
        
        List<HistoricEQData> candles = null ;
        HistoricEQData candle = null ;
        EquityMaster em = null ;
        
        em = getEquityMaster( record ) ;
        candles = ecRepo.findBySymbolAndDate( em.getSymbol(), date ) ;
        
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
        
        candle.setSymbol       ( em.getSymbol() ) ;
        candle.setOpen         ( Float.parseFloat( record[ 4] ) ) ;
        candle.setHigh         ( Float.parseFloat( record[ 5] ) ) ;
        candle.setLow          ( Float.parseFloat( record[ 6] ) ) ;
        candle.setClose        ( Float.parseFloat( record[ 7] ) ) ;
        candle.setPrevClose    ( Float.parseFloat( record[ 9] ) ) ;
        candle.setTotalTradeQty( Long.parseLong  ( record[11] ) ) ;
        candle.setTotalTradeVal( Float.parseFloat( record[12] ) ) ;
        candle.setTotalTrades  ( Long.parseLong  ( record[10] ) ) ;
        candle.setDate         ( date ) ;
        
        return candle ;
    }
}
