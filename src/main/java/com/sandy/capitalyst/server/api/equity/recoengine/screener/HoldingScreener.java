package com.sandy.capitalyst.server.api.equity.recoengine.screener;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
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
    
    private List<String> holdingNseSymbols = new ArrayList<>() ;
    
    public void initialize() throws Exception {
        
        EquityHoldingRepo ehRepo = getBean( EquityHoldingRepo.class ) ;
        List<EquityHolding> ehList = ehRepo.findNonZeroHoldings() ;
        
        for( EquityHolding eh : ehList ) {
            holdingNseSymbols.add( eh.getSymbolNse() ) ;
        }
    }

    @Override
    public ScreenerResult screen( EquityIndicators ind,
                                  List<EquityTechIndicator> techInds, 
                                  EquityReco recos ) {

        if( holdingNseSymbols.contains( ind.getSymbolNse() ) ) {
            return accept( msg( TEMPLATE, ind.getSymbolNse() ) ) ;
        }
        
        return nocare() ;
    }
}
