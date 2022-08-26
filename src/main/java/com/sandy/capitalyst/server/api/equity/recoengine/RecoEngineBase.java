package com.sandy.capitalyst.server.api.equity.recoengine;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.InputStream ;
import java.nio.charset.Charset ;
import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.beanutils.BeanUtils ;
import org.apache.log4j.Logger ;
import org.springframework.util.StreamUtils ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;
import com.sandy.capitalyst.server.api.equity.recoengine.cfg.AttributeEvaluatorCfg ;
import com.sandy.capitalyst.server.api.equity.recoengine.cfg.ScreenerCfg ;
import com.sandy.capitalyst.server.api.equity.recoengine.cfg.RecoEngineCfg ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTechIndicatorRepo ;

abstract class RecoEngineBase {
    
    private static final Logger log = Logger.getLogger( RecoEngineBase.class ) ;
    
    private static final String CFG_FILE_NAME        = "capitalyst-reco-engine.yaml" ;
    private static final String SCREENER_PKG_PREFIX  = "com.sandy.capitalyst.server.api.equity.recoengine.screener." ;
    private static final String EVALUATOR_PKG_PREFIX = "com.sandy.capitalyst.server.api.equity.recoengine.evaluator." ;
    
    private static boolean ENABLE_CFG_LOADING_LOG = false ;
    
    private RecoEngineCfg engineCfg = null ;
    private String lastConfigHash = null ;
    private long lastConfigLoadTime = 0 ;
    
    protected EquityIndicatorsRepo    eiRepo  = null ;
    protected EquityTechIndicatorRepo etiRepo = null ;
    protected EquityMasterRepo        emRepo  = null ;
    protected EquityHoldingRepo       ehRepo  = null ;
    
    protected List<Screener> screeners = new ArrayList<>() ;
    protected List<RecoAttributeEvaluator> evaluators = new ArrayList<>() ;

    protected RecoEngineBase() {}
    
    protected void initialize() throws Exception {
        
        String cfgContent = null ;
        String contentHash = null ;
        
        InputStream is = getRecoEngineConfigStream() ;
        
        cfgContent  = StreamUtils.copyToString( is, Charset.defaultCharset() ) ;
        contentHash = StringUtil.getHash( cfgContent ) ;
        
        is.close() ;
        
        log.debug( "Refreshing configuration and rebuilding reco engine." ) ;
        
        loadConfig( cfgContent ) ;
        lastConfigHash = contentHash ;
        
        refreshPipeline() ;
        
        eiRepo  = getBean( EquityIndicatorsRepo.class ) ;
        etiRepo = getBean( EquityTechIndicatorRepo.class ) ;
        emRepo  = getBean( EquityMasterRepo.class ) ;
        ehRepo  = getBean( EquityHoldingRepo.class ) ;
        
        lastConfigLoadTime = System.currentTimeMillis() ;
    }
    
    boolean needsInitiaization() throws Exception {
        
        if( secondsSinceLastConfigLoad() < 30 ) {
            return false ;
        }

        InputStream is = getRecoEngineConfigStream() ;
        
        String cfgContent  = StreamUtils.copyToString( is, Charset.defaultCharset() ) ;
        String contentHash = StringUtil.getHash( cfgContent ) ;
        
        is.close() ;
        
        if( StringUtil.isEmptyOrNull( lastConfigHash ) ||
            !lastConfigHash.equals( contentHash ) ) {
            return true ;
        }
        
        return false ;
    }
    
    private long secondsSinceLastConfigLoad() {
        return ( new Date().getTime() - lastConfigLoadTime )/1000 ;
    }
    
    private void loadConfig( String cfgContent ) throws Exception {
        
        ObjectMapper mapper = null ; 
        
        mapper = new ObjectMapper( new YAMLFactory() ) ; 
        mapper.findAndRegisterModules() ;
        
        engineCfg = mapper.readValue( cfgContent, RecoEngineCfg.class ) ;
    }
    
    private InputStream getRecoEngineConfigStream() 
        throws Exception {
        
        InputStream is = null ;
        
        File wkspDir = getConfig().getWorkspaceDir() ;
        File file = new File( wkspDir, "/config/" + CFG_FILE_NAME ) ;
        
        if( file.exists() ) {
            if( ENABLE_CFG_LOADING_LOG ) {
                log.debug( "Reco engine cfg found at " + file.getAbsolutePath() ) ; 
            }
            is = new FileInputStream( file ) ;
        }
        else {
            is = RecoEngineBase.class.getResourceAsStream( "/" + CFG_FILE_NAME ) ;
            if( is != null && ENABLE_CFG_LOADING_LOG ) {
                log.debug( "Reco engine cfg found in config classpath." ) ;
            }
        }
        
        return is ;
    }
    
    private void refreshPipeline() throws Exception {
        
        this.screeners.clear() ;
        this.evaluators.clear() ;
        
        for( ScreenerCfg cfg : engineCfg.getScreenerCfgs() ) {
            screeners.add( buildScreener( cfg ) ) ;
        }
        
        screeners.sort( new Comparator<Screener>() {
            @Override
            public int compare( Screener s1, Screener s2 ) {
                return s2.getPriority() - s1.getPriority() ;
            }
        } ) ;
        
        for( AttributeEvaluatorCfg cfg : engineCfg.getAttributeEvaluatorCfgs() ) {
            evaluators.add( buildComputer( cfg ) ) ;
        }
    }
    
    private Screener buildScreener( ScreenerCfg cfg ) throws Exception {
        
        String className = SCREENER_PKG_PREFIX + cfg.getId() ;
        Screener screener = (Screener)Class.forName( className )
                                     .getDeclaredConstructor()
                                     .newInstance() ;
        
        screener.setId( cfg.getId() ) ;
        screener.setPriority( cfg.getPriority() ) ;
        screener.setLowerLimit( cfg.getLowerLimit() ) ; 
        screener.setUpperLimit( cfg.getUpperLimit() ) ;
        
        if( StringUtil.isNotEmptyOrNull( cfg.getRange() ) ) {
            String[] parts = cfg.getRange().split( ":" ) ;
            screener.setLowerLimit( Float.parseFloat( parts[0].trim() ) ) ;
            screener.setUpperLimit( Float.parseFloat( parts[1].trim() ) ) ;
        }
        
        populateAttributes( cfg.getAttributes(), screener ) ;
        screener.initialize() ;

        return screener ;
    }
    
    private RecoAttributeEvaluator buildComputer( AttributeEvaluatorCfg cfg ) 
            throws Exception {
        
        RecoAttributeEvaluator evaluator = null ;
        
        String className = EVALUATOR_PKG_PREFIX + cfg.getId() ;
        evaluator = ( RecoAttributeEvaluator )Class.forName( className )
                                             .getDeclaredConstructor()
                                             .newInstance() ;
        
        populateAttributes( cfg.getAttributes(), evaluator ) ;
        evaluator.initialize() ;
        
        return evaluator ;
    }
    
    private void populateAttributes( Map<String, String> attributes, Object obj ) 
        throws Exception {
        
        if( attributes != null ) {
            for( String key : attributes.keySet() ) {
                String value = attributes.get( key ) ;
                BeanUtils.setProperty( obj, key, value ) ;
            }
        }
    }
}
