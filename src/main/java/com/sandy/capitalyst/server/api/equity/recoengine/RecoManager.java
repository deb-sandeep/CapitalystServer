package com.sandy.capitalyst.server.api.equity.recoengine;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringWriter ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang.StringUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.recoengine.EquityReco.Type ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.univocity.parsers.csv.CsvWriter ;
import com.univocity.parsers.csv.CsvWriterSettings ;

public class RecoManager {

    private static final Logger log = Logger.getLogger( RecoManager.class ) ;
    
    private static RecoManager instance = null ;
    
    private RecoEngine recoEngine = null ;
    private EquityIndicatorsRepo eiRepo = null ;

    // Key is the NSE symbol. The values are pre-sorted based on the natural 
    // ordering of the NSE symbols.
    private Map<String, EquityReco> recommendations = new TreeMap<>() ;
    private List<EquityReco> allRecos               = new ArrayList<>() ;
    private List<EquityReco> screenedRecos          = new ArrayList<>() ;
    
    private StatisticsManager statsMgr = new StatisticsManager() ;
    
    public static RecoManager instance() throws Exception {
        if( instance == null ) {
            instance = new RecoManager() ;
        }
        instance.initialize() ;
        return instance ;
    }
    
    // Note that this function will return a null in case a recommendation 
    // for the specified symbol does not exist. Recommendations are generated
    // for all symbols which have indicators associated with them (a row in 
    // the equity indicator table).
    public EquityReco getReco( String symbolNse ) {
        return recommendations.get( symbolNse ) ;
    }
    
    public List<EquityReco> getAllRecos() {
        return allRecos ;
    }
    
    public List<EquityReco> getScreenedRecos() {
        return screenedRecos ;
    }
    
    // Private functions
    
    private RecoManager() throws Exception {
        this.recoEngine = RecoEngine.instance() ;
    }
    
    private void initialize() throws Exception {
        
        if( recoEngine.needsInitiaization() ) {
            
            this.recoEngine.initialize() ;
            
            this.eiRepo   = getBean( EquityIndicatorsRepo.class ) ;
            this.statsMgr = new StatisticsManager() ;
            
            refreshRecommendationsCache() ;
        }
    }
    
    private void refreshRecommendationsCache() throws Exception {
        
        log.debug( "Refreshing recommendation manager cache" ) ;
        
        Iterable<EquityIndicators> inds = eiRepo.findAll() ;
        EquityReco reco = null ;
        
        recommendations.clear() ;
        allRecos.clear() ;
        screenedRecos.clear() ;
        
        for( EquityIndicators ind : inds ) {
            
            reco = recoEngine.screen( ind ) ;
            
            recommendations.put( ind.getSymbolNse(), reco ) ;
            allRecos.add( reco ) ;
            
            if( reco.getType() != Type.SCREENED_OUT ) {
                screenedRecos.add( reco ) ;
                statsMgr.assimilate( reco ) ;
            }
        }
        
        for( EquityReco r : screenedRecos ) {
            recoEngine.applyEvaluators( r, statsMgr ) ;
        }
        
        printStats() ;
    }
    
    private void printStats() {
        
        List<String> rejectedStocks = new ArrayList<>() ;
        List<String> screenedStocks = new ArrayList<>() ;
        
        for( String symbolNse : recommendations.keySet() ) {
            EquityReco reco = recommendations.get( symbolNse ) ;
            if( reco.getType() == Type.SCREENED_OUT ) {
                rejectedStocks.add( StringUtils.rightPad( symbolNse, 15 ) + 
                                    " - " + reco.getMessage() ) ;
            }
            else {
                screenedStocks.add( symbolNse ) ;
                statsMgr.assimilate( reco ) ;
            }
        }
        
        log.debug( "EquityReco manager statistics." ) ;
        log.debug( "  - Num recommendations = " + recommendations.size() ) ;
        log.debug( "  - Screened stocks     = " + screenedStocks.size() ) ;
        log.debug( "  - Rejected stocks     = " + rejectedStocks.size() ) ;
        
        //printGoodnessAttributes() ;
    }
    
    void printGoodnessAttributes() {
        
        StringWriter output = new StringWriter() ;
        CsvWriter csvWriter = new CsvWriter( output, new CsvWriterSettings() ) ;
        csvWriter.writeHeaders( 
                "Symbol", "Market Cap", "Beta", "CAGR EBIT", "PE",
                "PE Diff", "MC E Score", "MC Insight", "P Score" );
        
        for( EquityReco reco : screenedRecos ) {
            
            EquityIndicators ind = reco.getIndicators() ;
            
            csvWriter.addValue( reco.getSymbolNse() ) ;
            csvWriter.addValue( ind.getMarketCap() ) ;
            csvWriter.addValue( ind.getBeta() ) ;
            csvWriter.addValue( ind.getCagrEbit() ) ;
            csvWriter.addValue( ind.getPe() ) ;
            csvWriter.addValue( ind.getPe() - ind.getSectorPE() ) ;
            csvWriter.addValue( ind.getMcEssentialScore() ) ;
            csvWriter.addValue( ind.getMcInsightShort() ) ;
            csvWriter.addValue( ind.getPiotroskiScore() ) ;
            
            csvWriter.writeValuesToRow() ;
        }
        
        csvWriter.close() ;
        
        log.debug( output.toString() ) ;
    }
}
