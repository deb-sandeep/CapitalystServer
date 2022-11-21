package com.sandy.capitalyst.server.core;

import java.io.File ;

import org.springframework.beans.factory.annotation.Value ;
import org.springframework.boot.context.properties.* ;
import org.springframework.context.annotation.* ;

import lombok.Data ;

@Data
@Configuration( "config" )
@PropertySource( { "classpath:config/capitalyst.properties" } )
@ConfigurationProperties( "capitalyst" )
public class CapitalystConfig {

    private boolean runClassificationOnStartup    = false ;
    private boolean initializeRecoMgrOnStartup    = true ;
    private boolean updateAccountBalanceOnStartup = false ;
    
    private boolean classifyOnlyUnclassifiedEntries = false ;

    private File workspaceDir = null ;
    private File breezeCfgFile = null ;
    
    // A command line argument --devMode=true can be specified to set this 
    // value. If not set, devMode is set to false by default
    @Value( "${devMode:false}" ) 
    private boolean devMode = false ;
}
