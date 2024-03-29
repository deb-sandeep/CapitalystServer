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
    public static final String CFG_PRINT_API_CALL_LOG = "print_api_call_log" ;
    public static final String CFG_PRINT_EX_ORIGINS   = "print_exception_origins" ;
    public static final String CFG_NET_LOG_ENABLED    = "network_log_enabled" ;
    public static final String CFG_RATE_LIMIT_MINUTE  = "api_rate_limit_per_minute" ;
    public static final String CFG_FORCE_MKT_CLOSE    = "force_mkt_close" ;
    public static final String CFG_FORCE_MKT_OPEN     = "force_mkt_open" ;
    
    @Getter
    private boolean printAPIResponse = false ;
    
    @Getter
    private boolean networkLoggingEnabled = false ;
    
    @Getter
    private int rateLimitPerMinute = 75 ;
    
    @Getter
    private boolean printExceptionOrigins = true ;
    
    @Getter
    private boolean printAPICallLog = false ;
    
    @Getter
    private boolean forceMktClose = false ;
    
    @Getter
    private boolean forceMktOpen = false ;
    
    private NVPManager nvpMgr = null ;
    
    public BreezeNVPConfig() {
        loadNVPConfigValues() ;
    }
    
    private void loadNVPConfigValues() {
        
        nvpMgr = NVPManager.instance() ;
        
        printAPIResponse      = getBooleanCfg( CFG_PRINT_API_RESPONSE ) ;
        networkLoggingEnabled = getBooleanCfg( CFG_NET_LOG_ENABLED    ) ;
        rateLimitPerMinute    = getIntCfg    ( CFG_RATE_LIMIT_MINUTE  ) ;
        printExceptionOrigins = getBooleanCfg( CFG_PRINT_EX_ORIGINS   ) ;
        printAPICallLog       = getBooleanCfg( CFG_PRINT_API_CALL_LOG ) ;
        forceMktClose         = getBooleanCfg( CFG_FORCE_MKT_CLOSE    ) ;
        forceMktOpen          = getBooleanCfg( CFG_FORCE_MKT_OPEN     ) ;

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
            case CFG_PRINT_API_CALL_LOG:
                printAPICallLog = nvp.getBooleanValue() ;
                break ;
            case CFG_NET_LOG_ENABLED:
                networkLoggingEnabled = nvp.getBooleanValue() ;
                break ;
            case CFG_RATE_LIMIT_MINUTE:
                rateLimitPerMinute = nvp.getIntValue() ;
                break ;
            case CFG_PRINT_EX_ORIGINS:
                printExceptionOrigins = nvp.getBooleanValue() ;
                break ;
            case CFG_FORCE_MKT_CLOSE:
                forceMktClose = nvp.getBooleanValue() ;
                break ;
            case CFG_FORCE_MKT_OPEN:
                forceMktOpen = nvp.getBooleanValue() ;
                break ;
        }
    }
}
