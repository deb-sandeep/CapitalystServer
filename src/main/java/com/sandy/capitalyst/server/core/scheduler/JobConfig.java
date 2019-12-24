package com.sandy.capitalyst.server.core.scheduler;

import java.util.HashMap ;
import java.util.Map ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.sandy.common.util.StringUtil ;

public class JobConfig {
    
    private static final String CFG_VAR_PATTERN = "\\$\\{([^\\{]*)}+" ;

    private String identity = null ;
    private boolean active = true ;
    private String className = null ;
    private String description = null ;
    private String cronSchedule = null ;
    private Map<String, Object> jobData = new HashMap<>() ;
    
    public String getIdentity() {
        return identity ;
    }
    public void setIdentity( String identity ) {
        this.identity = identity ;
    }
    
    public boolean isActive() {
        return active ;
    }
    public void setActive( boolean active ) {
        this.active = active ;
    }
    
    public String getClassName() {
        return className ;
    }
    public void setClassName( String className ) {
        this.className = className ;
    }
    
    public String getDescription() {
        return description ;
    }
    public void setDescription( String description ) {
        this.description = description ;
    }
    
    public Map<String, Object> getJobData() {
        return jobData ;
    }
    public void setJobData( Map<String, Object> jobData ) {
        this.jobData = jobData ;
    }
    
    public String getCronSchedule() {
        return cronSchedule ;
    }
    public void setCronSchedule( String cronTrigger ) {
        this.cronSchedule = cronTrigger ;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder() ;
        builder.append( "JobConfig [\n" )
               .append( "   identity = " + this.identity + "\n" )
               .append( "   active = " + this.active + "\n" )
               .append( "   className = " + this.className + "\n" )
               .append( "   description = " + this.description + "\n" ) 
               .append( "   cronSchedule = " + this.cronSchedule + "\n" )  ;
        
        if( !jobData.isEmpty() ) {
            builder.append( "   jobData : \n" ) ;
            for( String key : jobData.keySet() ) {
                builder.append( "      " + key + " = " + jobData.get( key ) + "\n" ) ;
            }
        }
        
        builder.append( "]" ) ;
        return builder.toString() ;
    }
    
    public void enrichAttributes( Map<String, Object> envValues ) {
        this.identity = enrichString( this.identity, envValues ) ;
        this.className = enrichString( this.className, envValues ) ;
        this.description = enrichString( this.description, envValues ) ;
        
        for( String key : jobData.keySet() ) {
            Object value = jobData.get( key ) ;
            if( value instanceof String ) {
                String strVal = enrichString( (String)value, envValues ) ;
                jobData.put( key, strVal ) ;
            }
        }
    }

    private String enrichString( String input, Map<String, Object> envValues ) {
            
            if( StringUtil.isEmptyOrNull( input ) ) return input ;
            
            StringBuilder outputBuffer = new StringBuilder() ;
            
            Pattern r = Pattern.compile( CFG_VAR_PATTERN, Pattern.DOTALL ) ;
            Matcher m = r.matcher( input ) ;
            
            int lastEndMarker = 0 ;
            
            while( m.find() ) {
                int start = m.start() ;
                int end   = m.end() ;
                
                String processedString = processVar( input.substring( start, end ), envValues ) ;
                if( processedString != null ) {
                    outputBuffer.append( input.substring( lastEndMarker, start ) ) ;
                    outputBuffer.append( processedString ) ;
                    lastEndMarker = end ;
                }
            }
            
            outputBuffer.append( input.substring(lastEndMarker, input.length() ) ) ;
            return outputBuffer.toString() ;

        }

        private static String processVar( String input, 
                                          Map<String, Object> envValues ) {
            
            String varName = input.substring( 2, input.length()-1 ) ;
            try {
                Object varValue = envValues.get( varName ) ;
                if( varValue == null ) {
                    throw new RuntimeException( "Could not find variable - " + varName ) ;
                }
                return varValue.toString() ;
            }
            catch( Exception e ) {
                throw new RuntimeException( "Error processing var " + varName, e ) ;
            }
        }
}
