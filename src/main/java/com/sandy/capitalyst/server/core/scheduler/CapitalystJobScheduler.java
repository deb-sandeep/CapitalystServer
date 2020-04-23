package com.sandy.capitalyst.server.core.scheduler;

import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.quartz.CronScheduleBuilder ;
import org.quartz.CronTrigger ;
import org.quartz.Job ;
import org.quartz.JobBuilder ;
import org.quartz.JobDetail ;
import org.quartz.JobKey ;
import org.quartz.Scheduler ;
import org.quartz.TriggerBuilder ;
import org.quartz.impl.StdSchedulerFactory ;
import org.springframework.stereotype.Component ;

import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;

@Component
public class CapitalystJobScheduler {

    private static final Logger log = Logger.getLogger( CapitalystJobScheduler.class ) ;
    
    private ObjectMapper mapper = null ; 
    private Scheduler scheduler = null ;
    
    private Map<String, JobKey> jobKeyMap = new HashMap<String, JobKey>() ;
    
    public void initialize() throws Exception {
        if( scheduler == null ) {
            this.mapper = new ObjectMapper( new YAMLFactory() ) ; 
            this.mapper.findAndRegisterModules() ;
            this.scheduler = new StdSchedulerFactory().getScheduler() ;
            
            SchedulerConfig config = loadConfig() ;
            scheduler = StdSchedulerFactory.getDefaultScheduler() ;
            boolean jobsRegistered = false ;
            if( config.getJobConfigs() != null ) {
                for( JobConfig jobConfig : config.getJobConfigs() ) {
                    if( jobConfig.isActive() ) {
                        log.debug( "Registering job : " + jobConfig ) ;
                        registerJob( jobConfig ) ;
                        jobsRegistered = true ;
                    }
                    else {
                        log.debug( "Job : " + jobConfig + " is not active." ) ;
                    }
                }
            }
            
            if( jobsRegistered ) {
                scheduler.start() ;
            }
            else {
                log.info( "Not starting scheduler. No jobs available." ) ;
            }
        }
    }
    
    public void stop() throws Exception {
        scheduler.shutdown() ;
    }
    
    public void triggerJob( String jobName ) throws Exception {
        JobKey key = this.jobKeyMap.get( jobName ) ;
        if( key != null ) {
            this.scheduler.triggerJob( key ) ;
        }
        else {
            throw new IllegalArgumentException( "Job with the name " + 
                                                jobName + 
                                                " is not registered." ) ;
        }
    }
    
    private SchedulerConfig loadConfig() throws Exception {
        SchedulerConfig config =  null ;
        InputStream is = getClass().getResourceAsStream( "/scheduler-config.yaml" ) ;
        config = mapper.readValue( is, SchedulerConfig.class ) ;
        config.enrichValues() ;
        return config ;
    }
    
    private void registerJob( JobConfig config ) 
        throws Exception {

        JobDetail jobDetail = createJobDetail( config ) ;
        JobKey jobKey = jobDetail.getKey() ;
        CronTrigger trigger = createCronTrigger( config ) ;
        
        this.scheduler.scheduleJob( jobDetail, trigger ) ;
        this.jobKeyMap.put( jobKey.getName(), jobKey ) ;
    }
    
    @SuppressWarnings( "unchecked" )
    private JobDetail createJobDetail( JobConfig config ) 
        throws Exception {
        
        Class<? extends Job> jobClass = null ;
        JobBuilder jobBuilder = null ;
        JobDetail job = null ;
        JobKey jobKey = null ;
        
        jobKey = JobKey.jobKey( config.getIdentity() ) ;
        jobClass = ( Class<? extends Job> )Class.forName( config.getClassName() ) ;
        
        jobBuilder = JobBuilder.newJob( jobClass ) 
                               .withIdentity( jobKey )
                               .withDescription( config.getDescription() ) ;
        
        for( String key : config.getJobData().keySet() ) {
            Object value = config.getJobData().get( key ) ;
            if( value instanceof Boolean ) {
                jobBuilder.usingJobData( key, (Boolean)value ) ;
            }
            else if( value instanceof String ) {
                jobBuilder.usingJobData( key, (String)value ) ;
            }
            else if( value instanceof Integer ) {
                jobBuilder.usingJobData( key, (Integer)value ) ;
            }
        }
        
        job = jobBuilder.build() ;
        return job ;
    }
    
    private CronTrigger createCronTrigger( JobConfig config ) {
        return TriggerBuilder.newTrigger()
                      .withIdentity( "Trigger-" + config.getIdentity() )
                      .withDescription( "Trigger for " + config.getIdentity() )
                      .withSchedule( CronScheduleBuilder.cronSchedule( config.getCronSchedule() ) ) 
                      .build() ;
    }
}
