package com.sandy.capitalyst.server.core.scheduler;

import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.util.StringUtil ;

public class JobState {

    private Map<String, String> stateVariables = new HashMap<>() ;

    public Map<String, String> getStateVariables() {
        return stateVariables ;
    }

    public void setStateVariables( Map<String, String> stateVariables ) {
        this.stateVariables = stateVariables ;
    }
    
    public void setState( String key, Object value ) {
        stateVariables.put( key, value.toString() ) ;
    }
    
    public String getStateAsString( String key ) {
        String val = stateVariables.get( key ) ;
        return val ;
    }
    
    public Integer getStateAsInteger( String key ) {
        String val = stateVariables.get( key ) ;
        if( StringUtil.isNotEmptyOrNull( val ) ) {
            return Integer.valueOf( val.trim() ) ;
        }
        return null ;
    }
}
