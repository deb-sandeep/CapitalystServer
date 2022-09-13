package com.sandy.capitalyst.server.breeze;

import java.io.File ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;
import com.sandy.capitalyst.server.breeze.internal.BreezeConfig ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;

public class Breeze {

    private static final Logger log = Logger.getLogger( Breeze.class ) ;
    
    public static String ISO8601_FMT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static String ISO8601_FMT             = "yyyy-MM-dd'T'HH:mm:ss.000'Z'";
    public static final String BRZ_API_BASEURL   = "https://api.icicidirect.com/breezeapi/api/v1" ;
    
    private static Breeze instance = null ;
    
    public static Breeze instance() {
        if( instance == null ) {
            instance = new Breeze() ;
        }
        return instance ;
    }
    
    private List<BreezeCred> creds = new ArrayList<>() ;
    private Map<String, BreezeCred> credMap = new HashMap<>() ;
    private BreezeSessionManager sessionMgr = BreezeSessionManager.instance() ;
    private BreezeConfig config = null ;
    private List<BreezeAPIInvocationListener> listeners = new ArrayList<>() ;
    
    private boolean initialized = false ;
    
    private Breeze() {}
    
    public void initialize( File cfgFile ) throws Exception {
        log.debug( "Initializing Breeze" ) ;
        log.debug( "  Cfg path - " + cfgFile.getAbsolutePath() ) ;
        
        resetState() ;
        configure( cfgFile ) ;
        initialized = true ;
    }
    
    private void configure( File file ) throws Exception {
        
        ObjectMapper mapper = null ; 
        
        mapper = new ObjectMapper( new YAMLFactory() ) ; 
        mapper.findAndRegisterModules() ;
        
        this.config = mapper.readValue( file, BreezeConfig.class ) ;
        for( BreezeCred cred : this.config.getCredentials() ) {
            this.creds.add( cred ) ;
            this.credMap.put( cred.getUserId(), cred ) ;
        }
        
        this.sessionMgr.setCredentialsUpdated() ;
    }
    
    private void assertInitializedState() throws IllegalStateException {
        if( !initialized ) {
            throw new IllegalStateException( "Breeze not initialized." ) ;
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
        assertInitializedState() ;
        return this.config.getSerializationDir() ;
    }
    
    public List<BreezeCred> getAllCreds() {
        assertInitializedState() ;
        return this.creds ;
    }
    
    public BreezeCred getCred( String userId ) {
        assertInitializedState() ;
        return this.credMap.get( userId ) ;
    }
}