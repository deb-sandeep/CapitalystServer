package com.sandy.capitalyst.server.job.equity.eodrefresh;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.dao.equity.EquityDailyGain ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityDailyGainRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;

public class EquityDailyGainUpdater {
    
    static final Logger log = Logger.getLogger( EquityDailyGain.class ) ;

    private EquityDailyGainRepo edgRepo = null ;
    private EquityTxnRepo etRepo = null ;
    private EquityHoldingVOBuilder holdingVOBuilder = null ;
    
    public EquityDailyGainUpdater() {
        this.edgRepo = getBean( EquityDailyGainRepo.class ) ;
        this.etRepo = getBean( EquityTxnRepo.class ) ;
        this.holdingVOBuilder = new EquityHoldingVOBuilder() ;
    }

    public void updateEDG( EquityHolding h, HistoricEQData c ) {
        
        List<EquityTxn> txns = null ;
        IndividualEquityHoldingVO vo = null ;
        EquityDailyGain edg = null ;
        
        txns = etRepo.findByHoldingIdOrderByTxnDateAscActionAsc( h.getId() ) ;
        vo = holdingVOBuilder.buildVO( h, txns ) ;
        
        edg = edgRepo.findByHoldingAndDate( h, c.getDate() ) ;
        if( edg == null ) {
            edg = new EquityDailyGain() ;
            edg.setHolding( h ) ;
            edg.setDate( c.getDate() ) ;
        }
        
        int   quantity      = h.getQuantity() ;
        float investedValue = h.getAvgCostPrice() * h.getQuantity() ;
        float marketValue   = c.getClose() * h.getQuantity() ;
        float profit        = marketValue - investedValue ;
        
        edg.setQuantity( quantity ) ;
        edg.setInvestmentValue( investedValue ) ;
        edg.setClosingUnitPrice( c.getClose() ) ;
        edg.setMarketValue( marketValue ) ;
        edg.setProfit( profit ) ;
        
        populatePAT( vo, edg, c.getClose() ) ;
        populateDayChange( h, edg ) ;
        
        edgRepo.save( edg ) ;
    }
    
    private void populatePAT( IndividualEquityHoldingVO h, 
                              EquityDailyGain edg, 
                              float latestPrice ) {
        
        int stcgQty = h.getQuantity() - h.getLtcgQty() ;
        
        float ltcgCost = 0, ltcgValue = 0, ltcgProfit = 0, ltcgTax = 0 ;
        float stcgCost = 0, stcgValue = 0, stcgProfit = 0, stcgTax = 0 ;
        float sellBrokerage = 0 ;
        float taxAmount = 0 ;
        float valuePostTax = 0 ;
        float valueAtCost = 0 ;
        
        float valueAtMktPrice = h.getQuantity() * latestPrice ;
        
        if( h.getOwnerName().equals( "Sandeep" ) ) {
            sellBrokerage = (float)( valueAtMktPrice * (0.24/100)) ;
        }
        else {
            sellBrokerage = (float)( valueAtMktPrice * (0.77/100)) ;
        }
        
        ltcgCost   = h.getLtcgQty() * h.getAvgCostPrice() ;
        ltcgValue  = h.getLtcgQty() * latestPrice ;
        ltcgProfit = ltcgValue - ltcgCost ;
        ltcgTax    = ltcgProfit > 0 ? 0.1f * ltcgProfit : 0 ;
        
        stcgCost   = stcgQty * h.getAvgCostPrice() ;
        stcgValue  = stcgQty * latestPrice ;
        stcgProfit = stcgValue - stcgCost ;
        stcgTax    = stcgProfit > 0 ? 0.3f * stcgProfit : 0 ;
        
        valueAtCost = h.getQuantity() * h.getAvgCostPrice() ;
        
        taxAmount    = ltcgTax + stcgTax ;
        valuePostTax = valueAtMktPrice - taxAmount - sellBrokerage ;
        
        edg.setPat( valuePostTax - valueAtCost ) ;
        edg.setPatPct( ( edg.getPat() / valueAtCost ) * 100 ) ; 
    }
    
    private void populateDayChange( EquityHolding holding, 
                                    EquityDailyGain todayEDG ) {
        
        EquityDailyGain lastEDG = edgRepo.getLastEDG( holding.getId() ) ;
        
        if( lastEDG != null ) {
            
            // How much did the holding earn or lose today as compared to the
            // market value yesterday.
            float dayChange = todayEDG.getPat() - lastEDG.getPat() ;
            
            todayEDG.setDayChange( dayChange ) ;
            todayEDG.setDayChangePct( (dayChange/todayEDG.getInvestmentValue())*100 ) ;
            
            todayEDG.getHolding().setDayGain( dayChange ) ;
        }
    }
}
