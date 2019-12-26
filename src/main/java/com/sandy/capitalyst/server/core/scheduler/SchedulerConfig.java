package com.sandy.capitalyst.server.core.scheduler;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

public class SchedulerConfig {

    private Map<String, Object> envVars = new HashMap<>() ;
    private List<JobConfig> jobConfigs = new ArrayList<>() ;
    
    public Map<String, Object> getEnvVars() {
        return envVars ;
    }
    public void setEnvVars( Map<String, Object> envVars ) {
        this.envVars = envVars ;
    }
    
    public List<JobConfig> getJobConfigs() {
        return jobConfigs ;
    }
    public void setJobConfigs( List<JobConfig> jobConfigs ) {
        this.jobConfigs = jobConfigs ;
    }
    
    public void enrichValues() {
        if( jobConfigs != null ) {
            for( JobConfig jobCfg : jobConfigs ) {
                jobCfg.enrichAttributes( envVars ) ;
            }
        }
    }
}
