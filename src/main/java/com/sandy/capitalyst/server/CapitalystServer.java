package com.sandy.capitalyst.server ;

import org.apache.log4j.Logger ;
import org.springframework.beans.BeansException ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.boot.SpringApplication ;
import org.springframework.boot.autoconfigure.SpringBootApplication ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.ApplicationContextAware ;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer ;

import com.sandy.capitalyst.server.core.CapitalystConfig ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJobScheduler ;
import com.sandy.capitalyst.server.init.DaemonInitializer ;
import com.sandy.capitalyst.server.init.DevModeInitializer ;
import com.sandy.capitalyst.server.init.StartupTasksExecutor ;


@SpringBootApplication
public class CapitalystServer 
    implements ApplicationContextAware, WebMvcConfigurer {

    static final Logger log = Logger.getLogger( CapitalystServer.class ) ;
    
    private static ApplicationContext APP_CTX   = null ;
    private static CapitalystServer   APP       = null ;
    
    public static String CFG_GRP_APP = "Capitalyst" ;
    
    public static String CFG_RUN_CMP_DAEMON          = "run_portfolio_cmp_updater" ;
    public static String CFG_RUN_BATCH_DAEMON        = "run_batch_daemon" ;
    public static String CFG_RUN_EQ_HIST_EOD_DAEMON  = "run_eq_hist_eod_daemon" ;
    public static String CFG_RUN_IDX_HIST_EOD_DAEMON = "run_idx_hist_eod_daemon" ;
    public static String CFG_RUN_EQ_ITD_DAEMON       = "run_eq_itd_daemon" ;
    
    public static CapitalystConfig getConfig() {
        if( APP_CTX == null ) return null ;
        return (CapitalystConfig) APP_CTX.getBean( "config" ) ;
    }
    
    public static <T> T getBean( Class<T> type ) {
        return APP_CTX.getBean( type ) ;
    }

    public static ApplicationContext getAppContext() { return APP_CTX ; }

    public static CapitalystServer getApp() { return APP ; }
    
    // ---------------- Instance methods start ------------------------------
    @Autowired
    private CapitalystJobScheduler scheduler = null ;

    @Autowired
    private DevModeInitializer devModeInitializer = null ;

    @Autowired
    private StartupTasksExecutor startupTaskExecutor = null ;
    
    @Autowired
    private DaemonInitializer daemonInitializer = null ;
    
    public CapitalystServer() {
        APP = this ;
    }
    
    public CapitalystJobScheduler getScheduler() {
        return this.scheduler ;
    }
    
    public void initialize() throws Exception {
        devModeInitializer.initialize() ;
        startupTaskExecutor.initialize() ;
        daemonInitializer.initialize() ;
    }
    
    @Override
    public void setApplicationContext( ApplicationContext appCtx )
            throws BeansException {
        APP_CTX = appCtx ;
    }

    // --------------------- Main method ---------------------------------------
    public static void main( String[] args ) throws Exception {

        try {
            long startTime = System.currentTimeMillis() ;
            log.debug( "Starting Capitalyst Server.." ) ;

            log.debug( "Starting Spring Booot..." ) ;
            SpringApplication.run( CapitalystServer.class, args ) ;

            log.debug( "\n" ) ;
            log.debug( "Initializing Capitalyst Server App..." ) ;
            getBean( CapitalystServer.class ).initialize() ;
            long endTime = System.currentTimeMillis() ;

            int timeTaken = (int)(( endTime - startTime )/1000) ;

            log.debug( "" ) ;
            log.debug( "Capitalyst Server open for business. "  +
                       "Boot time = " + timeTaken + " secs." ) ;
            log.debug( "" ) ;
        }
        catch( Throwable t ) {
            log.error( "Capitalyst initialization failed. Error - " + t.getMessage() );
            t.printStackTrace() ;
        }
    }
}
