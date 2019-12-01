package com.sandy.capitalyst.server.config;

import java.io.File ;

import org.springframework.boot.context.properties.* ;
import org.springframework.context.annotation.* ;

@Configuration( "config" )
@PropertySource( "classpath:capitalyst.properties" )
@ConfigurationProperties( "capitalyst" )
public class CapitalystConfig {

    private boolean runClassificationOnStartup = false ;
    private boolean classifyOnlyUnclassifiedEntries = false ;
    private File workspaceDir = null ;
    
    public boolean isRunClassificationOnStartup() {
        return runClassificationOnStartup ;
    }
    
    public void setRunClassificationOnStartup( boolean bool ) {
        this.runClassificationOnStartup = bool ;
    }
    
    public boolean isClassifyOnlyUnclassifiedEntries() {
        return classifyOnlyUnclassifiedEntries ;
    }
    
    public void setClassifyOnlyUnclassifiedEntries( boolean bool ) {
        this.classifyOnlyUnclassifiedEntries = bool ;
    }
    
    public File getWorkspaceDir() {
        return workspaceDir ;
    }
    
    public void setWorkspaceDir( File workspaceDir ) {
        this.workspaceDir = workspaceDir ;
    }
}
