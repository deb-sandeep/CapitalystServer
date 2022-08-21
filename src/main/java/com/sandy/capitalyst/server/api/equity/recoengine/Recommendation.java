package com.sandy.capitalyst.server.api.equity.recoengine;

import java.util.List ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

import lombok.Getter ;

public class Recommendation {

    public static enum Type {
        ACCEPTANCE_CRITERIA_NOT_MET,
        IGNORE,
        BUY,
        STRONG_BUY,
        HOLD,
        SELL,
        STRONG_SELL
    }
    
    @Getter 
    private Type type = null ;
    
    @Getter 
    private String message = null ;
    
    @Getter
    private EquityIndicators indicators = null ;
    
    @Getter
    private List<EquityTechIndicator> techIndicators = null ;

    public void setReco( Type type, String msg,
                         EquityIndicators indicators,
                         List<EquityTechIndicator> techIndicators ) {
        
        this.type = type ;
        this.message = msg ;
        this.indicators = indicators ;
        this.techIndicators = techIndicators ;
    }
    
    public String getSymbolNse() {
        return this.indicators.getSymbolNse() ;
    }
}
