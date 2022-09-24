package com.sandy.capitalyst.server.breeze.internal;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.nvpconfig.NVPConfig ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigChangeListener ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;

import lombok.Getter ;

public class BreezeNVPConfig implements NVPConfigChangeListener {

    private static final Logger log = Logger.getLogger( BreezeNVPConfig.class ) ;

    public static final String CFG_GRP_NAME = "Breeze" ;
    
    public static final String CFG_PRINT_API_RESPONSE = "print_api_response" ;
    public static final String CFG_NET_LOG_ENABLED    = "network_log_enabled" ;
    
    @Getter
    private boolean printAPIResponse = false ;
    
    @Getter
    private boolean networkLoggingEnabled = false ;
    
    private NVPManager nvpMgr = null ;
    
    public BreezeNVPConfig() {
        
        loadConfigValues() ;
        nvpMgr.addConfigChangeListener( this, CFG_GRP_NAME ) ;
    }
    
    private void loadConfigValues() {
        
        nvpMgr = NVPManager.instance() ;
        
        printAPIResponse      = getBooleanCfg( CFG_PRINT_API_RESPONSE ) ;
        networkLoggingEnabled = getBooleanCfg( CFG_NET_LOG_ENABLED    ) ;
    }
    
    private boolean getBooleanCfg( String cfgName ) {
        
        return nvpMgr.getConfig( CFG_GRP_NAME, cfgName, "false" )
                     .getBooleanValue() ;
    }
    
    @Override
    public void propertyChanged( NVPConfig nvp ) {
        
        log.debug( "Breeze config " + nvp.getConfigName() + " changed." ) ;
        log.debug( "   New value = " + nvp.getValue() ) ;
        
        switch( nvp.getConfigName() ) {
            case CFG_PRINT_API_RESPONSE:
                printAPIResponse = nvp.getBooleanValue() ;
                break ;
            case CFG_NET_LOG_ENABLED:
                networkLoggingEnabled = nvp.getBooleanValue() ;
                break ;
        }
    }
}
