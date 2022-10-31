package com.sandy.capitalyst.server.api.equity.vo;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTrade ;

import lombok.Data ;

@Data
public class GraphData {
    
    @Data
    public static class TradeData {
        private Long x ;
        private Float y ;
        private Integer q ;
    }

    @Data
    public static class DayPriceData {
        private Long x ;
        private Float y ;
    }

    private List<Long>         labels       = new ArrayList<>() ;
    private List<Float>        eodPriceList = new ArrayList<>() ;
    private List<TradeData>    buyData      = new ArrayList<>() ;
    private List<TradeData>    sellData     = new ArrayList<>() ;
    private List<DayPriceData> avgData      = new ArrayList<>() ;
    private List<DayPriceData> cmpData      = new ArrayList<>() ;
    
    private Object           holding      = null ;
    private EquityMaster     equityMaster = null ;
    private EquityIndicators indicators   = null ;
    
    public void addTrade( EquityTrade et ) {
        
        TradeData t = new TradeData() ;
        
        t.setX( et.getTradeDate().getTime() ) ;
        t.setY( et.getValueAtCost() / et.getQuantity() ) ;
        t.setQ( et.getQuantity() ) ;
        
        if( et.getAction().equals( "Buy" ) ) {
            addTrade( buyData, t ) ;
        }
        else {
            addTrade( sellData, t ) ;
        }
    }
    
    private void addTrade( List<TradeData> tradeList, TradeData trade ) {

        boolean mergedWithExistingTrade = false ;
        
        for( TradeData existingTrade : tradeList ) {
            
            if( existingTrade.x.equals( trade.x ) ) {
                // There already exists a trade for this date, so 
                // we add the existing trade

                float investedAmt = existingTrade.q * existingTrade.y ;
                investedAmt += (trade.q * trade.y) ;
                
                existingTrade.q += trade.q ;
                existingTrade.y = investedAmt / existingTrade.q ;
                
                mergedWithExistingTrade = true ;
                break ;
            }
        }
        
        if( !mergedWithExistingTrade ) {
            tradeList.add( trade ) ;
        }
    }
}
