package com.sandy.capitalyst.server.api.equity.market.recoengine.filter;

import java.util.List ;

import com.sandy.capitalyst.server.api.equity.market.recoengine.EquityRecommendations ;
import com.sandy.capitalyst.server.api.equity.market.recoengine.Filter ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;

public class MarketCapFilter extends Filter {

    @Override
    public void filter( EquityIndicators ind,
                        List<EquityTechIndicator> techInds, 
                        EquityRecommendations recos ) {

        if( super.withinLimits( ind.getMarketCap() ) ) {
            
        }
    }

}
