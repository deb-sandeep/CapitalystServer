package com.sandy.capitalyst.server.test.standalone;

import com.sandy.capitalyst.server.core.CapitalystConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan("com.sandy.capitalyst.server")
public class CapitalystStandaloneDriver {

    static final Logger log = Logger.getLogger( CapitalystStandaloneDriver.class ) ;
    
    private ConfigurableApplicationContext appCtx = null ;

    public CapitalystConfig getConfig() {
        return ( CapitalystConfig ) appCtx.getBean( "config" ) ;
    }
    
    public <T> T getBean( Class<T> type ) {
        return appCtx.getBean( type ) ;
    }

    public ApplicationContext getAppContext() { return appCtx; }

    public void initializeDriver( String[] args ) throws Exception {
        log.debug( "Starting Capitalyst Server Driver.." ) ;
        log.debug( "Starting Spring Booot..." ) ;
        this.appCtx = SpringApplication.run( CapitalystStandaloneDriver.class, args ) ;
        log.debug( "Spring Spring Booot loaded..." ) ;
    }

    public void shutdownDriver() throws Exception {
        SpringApplication.exit( this.appCtx, () -> 0 ) ;
        System.exit(0);
    }
}
