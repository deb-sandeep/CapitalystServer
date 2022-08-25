package com.sandy.capitalyst.server.api.equity.recoengine;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;

import lombok.Getter ;

public class StatisticsManager {

    static final Logger log = Logger.getLogger( StatisticsManager.class ) ;
    
    @Getter private DescriptiveStatistics mktCapStats      = new DescriptiveStatistics() ;
    @Getter private DescriptiveStatistics betaStats        = new DescriptiveStatistics() ;
    @Getter private DescriptiveStatistics cagrStats        = new DescriptiveStatistics() ;
    @Getter private DescriptiveStatistics peStats          = new DescriptiveStatistics() ;
    @Getter private DescriptiveStatistics piotroskiStats   = new DescriptiveStatistics() ;
    @Getter private DescriptiveStatistics mcEssentialStats = new DescriptiveStatistics() ;
    
    public void assimilate( EquityReco reco ) {
        
        EquityIndicators ind = reco.getIndicators() ;
        
        if( !reco.getEquityMaster().isEtf() ) {
            
            mktCapStats.addValue( ind.getMarketCap() ) ;
            betaStats.addValue( ind.getBeta() ) ;
            cagrStats.addValue( ind.getCagrEbit() ) ;
            peStats.addValue( ind.getPe() ) ;
            piotroskiStats.addValue( ind.getPiotroskiScore() ) ;
            mcEssentialStats.addValue( ind.getMcEssentialScore() ) ;
        }
    }
}
