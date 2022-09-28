package com.sandy.capitalyst.server.job.equity.eodrefresh;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.equity.EquityDailyGain ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityDailyGainRepo ;

public class EquityDailyGainUpdater {
    
    static final Logger log = Logger.getLogger( EquityDailyGain.class ) ;

    private EquityDailyGainRepo edgRepo = null ;
    
    public EquityDailyGainUpdater() {
        this.edgRepo = getBean( EquityDailyGainRepo.class ) ;
    }

    public void updateEDG( EquityHolding h, HistoricEQData c ) {
        
        EquityDailyGain edg = null ;
        
        edg = edgRepo.findByHoldingAndDate( h, c.getDate() ) ;
        if( edg == null ) {
            edg = new EquityDailyGain() ;
            edg.setHolding( h ) ;
            edg.setDate( c.getDate() ) ;
        }
        
        int   quantity      = h.getQuantity() ;
        float investedValue = h.getAvgCostPrice() * h.getQuantity() ;
        float dayUnitChange = c.getClose() - c.getPrevClose() ;
        float dayChange     = dayUnitChange * h.getQuantity() ;
        float marketValue   = c.getClose() * h.getQuantity() ;
        
        edg.setQuantity( quantity ) ;
        edg.setInvestmentValue( investedValue ) ;
        edg.setClosingUnitPrice( c.getClose() ) ;
        edg.setMarketValue( marketValue ) ;
        edg.setDayChange( dayChange ) ;
        edg.setDayChangePct( (dayUnitChange/c.getPrevClose() )*100 ) ;
        
        edg.getHolding().setDayGain( dayChange ) ;
        
        edgRepo.save( edg ) ;
    }
}
