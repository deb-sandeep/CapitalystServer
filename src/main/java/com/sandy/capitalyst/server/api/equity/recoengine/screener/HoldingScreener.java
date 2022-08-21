package com.sandy.capitalyst.server.api.equity.recoengine.screener;

import java.util.List ;

import com.sandy.capitalyst.server.api.equity.recoengine.Recommendation ;
import com.sandy.capitalyst.server.api.equity.recoengine.Screener ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

/**
 * Forces inclusion of all the holding symbols.
 * 
 * Configuration parameters: NONE
 */
public class HoldingScreener extends Screener {
    
    private static final String TEMPLATE = "{id} - Including {0}." ;

    @Override
    public ScreenerResult screen( EquityIndicators ind,
                                List<EquityTechIndicator> techInds, 
                                Recommendation recos ) {

        EquityHoldingRepo ehRepo = getBean( EquityHoldingRepo.class ) ;
        
        List<EquityHolding> ehList = null ;
        ehList = ehRepo.findNonZeroHoldingsForNSESymbol( ind.getSymbolNse() ) ;
        
        if( ehList != null && !ehList.isEmpty() ) {
            return accept( msg( TEMPLATE, ind.getSymbolNse() ) ) ;
        }
        
        return nocare() ;
    }

}
