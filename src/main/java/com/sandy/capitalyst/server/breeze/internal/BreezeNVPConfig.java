package com.sandy.capitalyst.server.breeze.internal;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfig ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigChangeListener ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;

import lombok.Getter ;

public class BreezeNVPConfig implements NVPConfigChangeListener {

    private static final Logger log = Logger.getLogger( BreezeNVPConfig.class ) ;

    public static final String CFG_GRP_NAME = "Breeze" ;
    
    public static final String CFG_PRINT_API_RESPONSE = "print_api_response" ;
    public static final String CFG_NET_LOG_ENABLED    = "network_log_enabled" ;
    public static final String CFG_RATE_LIMIT_MINUTE  = "rate_limit_per_minute" ;
    
    @Getter
    private boolean printAPIResponse = false ;
    
    @Getter
    private boolean networkLoggingEnabled = false ;
    
    @Getter
    private int rateLimitPerMinute = 75 ;
    
    private NVPManager nvpMgr = null ;
    
    public BreezeNVPConfig() {
        
        if( CapitalystServer.isInServerMode() ) {
            loadNVPConfigValues() ;
        }
    }
    
    private void loadNVPConfigValues() {
        
        nvpMgr = NVPManager.instance() ;
        
        printAPIResponse      = getBooleanCfg( CFG_PRINT_API_RESPONSE ) ;
        networkLoggingEnabled = getBooleanCfg( CFG_NET_LOG_ENABLED    ) ;
        rateLimitPerMinute    = getIntCfg    ( CFG_RATE_LIMIT_MINUTE  ) ;

        nvpMgr.addConfigChangeListener( this, CFG_GRP_NAME ) ;
    }
    
    private boolean getBooleanCfg( String cfgName ) {
        
        return nvpMgr.getConfig( CFG_GRP_NAME, cfgName, "false" )
                     .getBooleanValue() ;
    }
    
    private int getIntCfg( String cfgName ) {
        
        return nvpMgr.getConfig( CFG_GRP_NAME, cfgName, "0" )
                     .getIntValue() ;
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
            case CFG_RATE_LIMIT_MINUTE:
                rateLimitPerMinute = nvp.getIntValue() ;
                break ;
        }
    }
}
