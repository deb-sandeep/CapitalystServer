package com.sandy.capitalyst.server.api.equity.market.recoengine.filter;

import java.util.List ;

import com.sandy.capitalyst.server.api.equity.market.recoengine.EquityRecommendations ;
import com.sandy.capitalyst.server.api.equity.market.recoengine.Filter ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

public class MarketCapFilter extends Filter {
    
    private static final String TEMPLATE = 
            "MarketCapFilter {0}. Value = {1}, Lower limit = {2}." ;

    @Override
    public FilterResult filter( EquityIndicators ind,
                                List<EquityTechIndicator> techInds, 
                                EquityRecommendations recos ) {
        
        String str = msg( TEMPLATE, null, 
                          ind.getMarketCap(), 
                          super.getLowerLimit() ) ;
        
        if( super.withinLimits( ind.getMarketCap() ) ) {
            return accept( msg( str, "Accept" ) ) ;
        }
        return reject( msg( str, "Reject" ) ) ;
    }

}
