package com.sandy.capitalyst.server.api.equity.recoengine.screener;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.api.equity.recoengine.Screener ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo ;
import com.sandy.common.util.StringUtil ;

/**
 * Excludes any equity which is not a part of the indexes specified.
 * 
 * Configuration parameters:
 * 
 * includedIndexes : [MANDATORY]
 *  Comma separated list of NSE indexes 
 */
public class IndexScreener extends Screener {
    
    static final Logger log = Logger.getLogger( IndexScreener.class ) ;
    
    private static final String TEMPLATE = "{id} - Rejecting {0}." ;
    
    private List<String> indexesForInclusion = new ArrayList<>() ;
    private List<String> eqForInclusion = new ArrayList<>() ;
    
    // This is populated through the yaml configuration
    public void setIncludedIndexes( String indexNames ) {
        
        if( StringUtil.isNotEmptyOrNull( indexNames ) ) {
            String[] parts = indexNames.split( "," ) ;
            for( String part : parts ) {
                indexesForInclusion.add( part.trim() ) ;
            }
        }
    }

    public void initialize() throws Exception {
        
        IndexEquityRepo ieRepo = getBean( IndexEquityRepo.class ) ;
        
        for( String idxName : indexesForInclusion ) {
            eqForInclusion.addAll( ieRepo.findEquitiesForIndex( idxName ) ) ;
        }
    }
    
    @Override
    public ScreenerResult screen( EquityIndicators ind,
                                  List<EquityTechIndicator> techInds, 
                                  EquityReco recos ) {
        
        if( eqForInclusion.contains( ind.getSymbolNse() ) ) {
            return nocare() ;
        }
        return reject( msg( TEMPLATE, ind.getSymbolNse() ) ) ;
    }

}
