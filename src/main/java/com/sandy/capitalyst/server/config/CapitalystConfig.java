package com.sandy.capitalyst.server.config;

import org.springframework.boot.context.properties.* ;
import org.springframework.context.annotation.* ;

@Configuration( "config" )
@PropertySource( "classpath:capitalyst.properties" )
@ConfigurationProperties( "capitalyst" )
public class CapitalystConfig {

    private boolean testProperty = false ;

    public boolean isTestProperty() {
        return testProperty ;
    }

    public void setTestProperty( boolean testProperty ) {
        this.testProperty = testProperty ;
    }
}
