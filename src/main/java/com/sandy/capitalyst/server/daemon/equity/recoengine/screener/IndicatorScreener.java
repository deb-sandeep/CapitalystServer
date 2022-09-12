package com.sandy.capitalyst.server.daemon.equity.recoengine.screener;

import java.util.ArrayList ;
import java.util.List ;

import com.sandy.capitalyst.server.daemon.equity.recoengine.EquityReco ;
import com.sandy.capitalyst.server.daemon.equity.recoengine.internal.Screener ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.common.util.StringUtil ;

/**
 * Filters out those stocks which don't lie within the specified 
 * indicator ranges.
 * 
 * Configuration parameters:
 * 
 * includedMCInsights : [OPTIONAL]
 *     Default value : null
 *     A list of comma separated MC Insights values (excluding the PERFORMER
 *     suffix) which should be included. 
 *     
 * minMcEssentialScore : [OPTIONAL]
 *     Default value : 0
 *     Minimum MC Essentials score. 
 *     
 * minMarketCap : [OPTIONAL]
 *     Default value : 0
 *     Minimum market cap in crores
 *     
 * betaRange : [OPTIONAL]
 *     Default value : -Inf to +Inf
 *     A range of beta values for qualification. Both end markers are included.
 *     
 * minCagrEbit : [OPTIONAL]
 *     Default value : -Inf
 *     Minimum CAGR EBIT for qualification
 *     
 * minPiotroskiScore : [OPTIONAL]
 *     Default value : 0
 *     Minimum Piotroski score
 */
public class IndicatorScreener extends Screener {
    
    private List<String> includedMCInsights   = new ArrayList<>() ;
    private int          minMCEssentialsScore = 0 ;
    private int          minMarketCap         = 0 ;
    private float        minBeta              = Float.NEGATIVE_INFINITY ;
    private float        maxBeta              = Float.POSITIVE_INFINITY ;
    private float        minCagrEbit          = Float.NEGATIVE_INFINITY ;
    private int          minPiotroskiScore    = 0 ;
    
    public void setIncludedMCInsights( String val ) {
        if( StringUtil.isNotEmptyOrNull( val ) ) {
            String[] parts = val.split( "," ) ;
            for( String part : parts ) {
                includedMCInsights.add( part.trim() + " PERFORMER" ) ;
            }
        }
    }
    
    public void setMinMcEssentialScore( int val ) {
        this.minMCEssentialsScore = val ;
    }
    
    public void setMinMarketCap( int val ) {
        this.minMarketCap = val ;
    }
    
    public void setBetaRange( String range ) {
        if( StringUtil.isNotEmptyOrNull( range ) ) {
            String[] parts = range.split( ":" ) ;
            minBeta = Float.parseFloat( parts[0] ) ;
            maxBeta = Float.parseFloat( parts[1] ) ;
        }
    }
    
    public void setMinCagrEbit( float val ) {
        this.minCagrEbit = val ;
    }
    
    public void setMinPiotroskiScore( int val ) {
        this.minPiotroskiScore = val ;
    }

    @Override
    public ScreenerResult screen( EquityIndicators ind,
                                List<EquityTechIndicator> techInds, 
                                EquityReco recos ) {
        
        if( !includedMCInsights.contains( ind.getMcInsightShort() ) ) {
            return reject( "MC insights out of range. " + ind.getMcInsightShort() ) ;
        }
        
        if( ind.getMcEssentialScore() < this.minMCEssentialsScore ) {
            return reject( "MC essential score disqualified. " + ind.getMcEssentialScore() ) ;
        }
        
        if( ind.getMarketCap() < this.minMarketCap ) {
            return reject( "Market cap disqualified. " + ind.getMarketCap() ) ;
        }
        
        if( ind.getBeta() < this.minBeta || ind.getBeta() > this.maxBeta ) {
            return reject( "Beta not within range. " + ind.getBeta() ) ;
        }
        
        if( ind.getCagrEbit() < this.minCagrEbit ) {
            return reject( "CAGR EBIT disqualified. " + ind.getCagrEbit() ) ;
        }
        
        if( ind.getPiotroskiScore() < this.minPiotroskiScore ) {
            return reject( "Piotroski score disqualifed. " + ind.getPiotroskiScore() ) ;
        }
        
        return accept( "Indicator screeners passed." ) ;
    }

}
