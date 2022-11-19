package com.sandy.capitalyst.server.daemon.equity.recoengine;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository.LTP ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTTMPerf ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

import lombok.Data ;

@Data
public class EquityReco {

    public static enum Type {
        SCREENED_OUT,
        IGNORE,
        STRONG_BUY,
        BUY,
        HOLD,
        REDUCE,
        STRONG_SELL
    }
    
    private Type type = null ;
    private String message = null ;
    
    private EquityMaster              equityMaster   = null ;
    private EquityIndicators          indicators     = null ;
    private List<EquityHolding>       holdings       = new ArrayList<>() ;
    private List<EquityTechIndicator> techIndicators = null ;
    private EquityTTMPerf             ttmPerf        = null ;
    private LTP                       ltp            = null ;
    
    private float goodnessScore = 0 ;
    private boolean monitored = false ;

    public void setReco( Type type, String msg ) {
        
        this.type = type ;
        this.message = msg ;
    }
    
    public String getSymbolNse() {
        return this.indicators.getSymbolNse() ;
    }
    
    public boolean isInPortfolio() {
        return !this.holdings.isEmpty() ;
    }
}
