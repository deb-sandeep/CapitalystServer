package com.sandy.capitalyst.server.core;

import java.io.File ;

import org.springframework.boot.context.properties.* ;
import org.springframework.context.annotation.* ;

import lombok.Data ;

@Data
@Configuration( "config" )
@PropertySource( "classpath:capitalyst.properties" )
@ConfigurationProperties( "capitalyst" )
public class CapitalystConfig {

    private boolean runClassificationOnStartup = false ;
    private boolean classifyOnlyUnclassifiedEntries = false ;
    private File    workspaceDir = null ;
    private boolean batchDaemonEnabled = false ;
}