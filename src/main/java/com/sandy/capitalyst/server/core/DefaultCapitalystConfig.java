package com.sandy.capitalyst.server.core;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

public class DefaultCapitalystConfig extends CapitalystConfig {

    public DefaultCapitalystConfig() {
        super.setWorkspaceDir( new File( "/Users/sandeep/projects/workspace/capitalyst" ) ) ;
        super.setBreezeCfgFile( new File( "/Users/sandeep/projects/workspace/capitalyst/breeze/config/breeze-config.yaml" ) ) ;
        super.setDevMode( true ) ;
    }
}
