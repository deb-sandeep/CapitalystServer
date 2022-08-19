package com.sandy.capitalyst.server.api.equity.market.recoengine;

import static com.sandy.capitalyst.server.CapitalystServer.getConfig ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.beanutils.BeanUtils ;
import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;
import com.sandy.capitalyst.server.api.equity.market.recoengine.cfg.AttributeComputerCfg ;
import com.sandy.capitalyst.server.api.equity.market.recoengine.cfg.FilterCfg ;
import com.sandy.capitalyst.server.api.equity.market.recoengine.cfg.RecoEngineCfg ;
import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityTechIndicator ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTechIndicatorRepo ; 

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

public class RecoEngine {
    
    private static final Logger log = Logger.getLogger( RecoEngine.class ) ;
    
    private static final String CFG_FILE_NAME = "capitalyst-reco-engine.yaml" ;
    private static final String FILTER_PKG_PREFIX = "com.sandy.capitalyst.server.api.equity.market.recoengine.filter." ;
    private static final String COMP_PKG_PREFIX = "com.sandy.capitalyst.server.api.equity.market.recoengine.attrcomputer." ;
    
    private RecoEngineCfg engineCfg = null ;
    private File cfgFile = null ;
    private long lastCfgLoadTime = 0 ;
    
    private List<Filter> filters = new ArrayList<>() ;
    private List<AttributeComputer> computers = new ArrayList<>() ;
    
    private EquityTechIndicatorRepo etiRepo = null ;

    public RecoEngine() {}
    
    public void initialize() throws Exception {
        if( configRefreshRequired() ) {
            log.debug( "Initializing RecoEngine" ) ;
            loadConfig() ;
            refreshPipeline() ;
            
            etiRepo = getBean( EquityTechIndicatorRepo.class ) ;
        }
    }
    
    public EquityRecommendations getRecommendations( EquityIndicators ind ) {
        
        EquityRecommendations recos = null ;
        List<EquityTechIndicator> techInds = null ;
        
        techInds = etiRepo.findByIsin( ind.getIsin() ) ;
        
        for( Filter filter : filters ) {
            filter.filter( ind, techInds, recos ) ;
        }
        
        return null ;
    }
    
    // Private functions
    private boolean configRefreshRequired() {
        
        if( engineCfg == null ) {
            return true ;
        }
        else {
            if( cfgFile != null ) {
                long lmt = cfgFile.lastModified() ;
                if( lmt > lastCfgLoadTime ) {
                    return true ;
                }
            }
        }
        return false ;
    }
    
    private void loadConfig() throws Exception {
        
        ObjectMapper mapper = null ; 
        InputStream is = null ;
        
        mapper = new ObjectMapper( new YAMLFactory() ) ; 
        mapper.findAndRegisterModules() ;
        
        is = getRecoEngineConfigStream() ;
        engineCfg = mapper.readValue( is, RecoEngineCfg.class ) ;
        lastCfgLoadTime = new Date().getTime() ;
        is.close() ;
    }
    
    private InputStream getRecoEngineConfigStream() 
        throws Exception {
        
        InputStream is = null ;
        CapitalystConfig cfg = getConfig() ;
        
        if( cfg == null ) {
            is = RecoEngine.class.getResourceAsStream( "/" + CFG_FILE_NAME ) ;
            if( is != null ) {
                log.debug( "Reco engine cfg found in config classpath." ) ;
            }
        }
        else {
            File wkspDir = getConfig().getWorkspaceDir() ;
            File file = new File( wkspDir, "/config/" + CFG_FILE_NAME ) ;
            
            if( file.exists() ) {
                log.debug( "Reco engine cfg found at " + file.getAbsolutePath() ) ; 
                cfgFile = file ;
                is = new FileInputStream( file ) ;
            }
            else {
                is = RecoEngine.class.getResourceAsStream( "/" + CFG_FILE_NAME ) ;
                if( is != null ) {
                    log.debug( "Reco engine cfg found in config classpath." ) ;
                }
            }
        }
        
        return is ;
    }
    
    private void refreshPipeline() throws Exception {
        
        this.filters.clear() ;
        this.computers.clear() ;
        
        for( FilterCfg cfg : engineCfg.getFilterCfgs() ) {
            filters.add( buildFilter( cfg ) ) ;
        }
        
        for( AttributeComputerCfg cfg : engineCfg.getAttributeComputerCfgs() ) {
            computers.add( buildComputer( cfg ) ) ;
        }
    }
    
    private Filter buildFilter( FilterCfg cfg ) throws Exception {
        
        String className = FILTER_PKG_PREFIX + cfg.getId() ;
        Filter filter = (Filter)Class.forName( className )
                                     .getDeclaredConstructor()
                                     .newInstance() ;
        
        filter.setPriority( cfg.getPriority() ) ;
        filter.setLowerLimit( cfg.getLowerLimit() ) ; 
        filter.setUpperLimit( cfg.getUpperLimit() ) ;
        
        if( StringUtil.isNotEmptyOrNull( cfg.getRange() ) ) {
            String[] parts = cfg.getRange().split( ":" ) ;
            filter.setLowerLimit( Float.parseFloat( parts[0].trim() ) ) ;
            filter.setUpperLimit( Float.parseFloat( parts[1].trim() ) ) ;
        }
        
        populateAttributes( cfg.getAttributes(), filter ) ;

        return filter ;
    }
    
    private AttributeComputer buildComputer( AttributeComputerCfg cfg ) 
            throws Exception {
        
        AttributeComputer computer = null ;
        
        String className = COMP_PKG_PREFIX + cfg.getId() ;
        computer = ( AttributeComputer )Class.forName( className )
                                             .getDeclaredConstructor()
                                             .newInstance() ;
        
        computer.setWeight( cfg.getWeight() ) ;
        
        populateAttributes( cfg.getAttributes(), computer ) ;
        
        return computer ;
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
    
    public static void main( String[] args ) throws Exception {
        RecoEngine engine = new RecoEngine() ;
        engine.initialize() ;
    }
}
