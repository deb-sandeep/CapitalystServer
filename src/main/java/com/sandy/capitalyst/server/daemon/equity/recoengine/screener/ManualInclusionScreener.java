package com.sandy.capitalyst.server.daemon.equity.recoengine.screener;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.Screener ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.common.util.StringUtil ;

/**
 * Forces inclusion of the configured NSE symbol.
 * 
 * Configuration parameters:
 * 
 * nseSymbols : [MANDATORY]
 *  Comma separated list of NSE symbols which should not be filtered out
 */
public class ManualInclusionScreener extends Screener {
    
    private static final String TEMPLATE = "{id} - Including {0}." ;
    
    private List<String> nseSymbolsForInclusion = new ArrayList<>() ;
    
    // This is populated through the yaml configuration
    public void setNseSymbols( String symbols ) {
        
        if( StringUtil.isNotEmptyOrNull( symbols ) ) {
            String[] parts = symbols.split( "," ) ;
            for( String part : parts ) {
                nseSymbolsForInclusion.add( part.trim().toUpperCase() ) ;
            }
        }
    }

    @Override
    public ScreenerResult screen( EquityIndicators ind,
                                List<EquityTechIndicator> techInds, 
                                EquityReco recos ) {
        
        if( nseSymbolsForInclusion.isEmpty() ) {
            throw new IllegalStateException( "nseSymbols not configured." ) ;
        }

        if( nseSymbolsForInclusion.contains( ind.getSymbolNse() ) ) {
            return accept( msg( TEMPLATE, ind.getSymbolNse() ) ) ;
        }
        
        return nocare() ;
    }

}
