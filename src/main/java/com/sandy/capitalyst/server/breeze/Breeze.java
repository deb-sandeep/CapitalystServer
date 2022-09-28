package com.sandy.capitalyst.server.breeze;

import java.io.File ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;
import com.sandy.capitalyst.server.breeze.internal.BreezeExternalConfig ;
import com.sandy.capitalyst.server.breeze.internal.BreezeNVPConfig ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager.BreezeSession ;

public class Breeze {

    private static final Logger log = Logger.getLogger( Breeze.class ) ;
    
    public static String ISO8601_FMT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String ISO8601_FMT             = "yyyy-MM-dd'T'HH:mm:ss.000'Z'";
    public static final String BRZ_API_BASEURL   = "https://api.icicidirect.com/breezeapi/api/v1" ;
    
    public static String CFG_RUN_CMP_DAEMON   = "run_portfolio_cmp_updater" ;
    public static String CFG_RUN_BATCH_DAEMON = "run_batch_daemon" ;
    
    private static Breeze instance = null ;
    
    public static Breeze instance() {
        if( instance == null ) {
            instance = new Breeze() ;
        }
        return instance ;
    }
    
    public static BreezeNVPConfig config() {
        return instance().getNVPCfg() ;
    }
    
    private List<BreezeCred> creds = new ArrayList<>() ;
    
    private Map<String, BreezeCred> credMap = new HashMap<>() ;
    
    private BreezeSessionManager sessionMgr = BreezeSessionManager.instance() ;
    
    private BreezeExternalConfig config = null ;
    
    private List<BreezeAPIInvocationListener> listeners = new ArrayList<>() ;
    
    private BreezeNVPConfig nvpCfg = null ;
    
    private Breeze() {
        nvpCfg = new BreezeNVPConfig() ;
    }
    
    public void initialize( File cfgFile ) throws Exception {
        
        log.debug( "Initializing Breeze" ) ;
        log.debug( "  Cfg path - " + cfgFile.getAbsolutePath() ) ;
        
        resetState() ;
        configure( cfgFile ) ;
    }
    
    private void configure( File file ) throws Exception {
        
        ObjectMapper mapper = null ;
        BreezeSession session = null ;
        BreezeSessionManager sessionMgr = null ;
        
        sessionMgr = BreezeSessionManager.instance() ;
        
        mapper = new ObjectMapper( new YAMLFactory() ) ; 
        mapper.findAndRegisterModules() ;
        
        this.config = mapper.readValue( file, BreezeExternalConfig.class ) ;
        
        for( BreezeCred cred : this.config.getCredentials() ) {
            this.creds.add( cred ) ;
            this.credMap.put( cred.getUserId(), cred ) ;
            
            log.debug( cred ) ;
            
            session = sessionMgr.getSession( cred ) ;
            if( !session.getCred().getAppKey().equals( cred.getAppKey() ) ) {
                log.debug( "Detected credential change. Invalidating existing session state." ) ;
                sessionMgr.invalidateSession( cred ) ;
            }
        }
    }
    
    private void resetState() {
        this.creds.clear() ; 
        this.credMap.clear() ;
    }
    
    public void addInvocationListener( BreezeAPIInvocationListener l ) {
        this.listeners.add( l ) ;
    }
    
    public List<BreezeAPIInvocationListener> getListeners() {
        return this.listeners ;
    }
    
    public File getSerializationDir() {
        return this.config.getSerializationDir() ;
    }
    
    public List<BreezeCred> getAllCreds() {
        return this.creds ;
    }
    
    public BreezeCred getCred( String userId ) {
        return this.credMap.get( userId ) ;
    }
    
    public BreezeNVPConfig getNVPCfg() {
        return this.nvpCfg ;
    }
    
    public boolean hasActiveSession( BreezeCred cred ) {
        return sessionMgr.hasActiveSession( cred ) ;
    }
}
