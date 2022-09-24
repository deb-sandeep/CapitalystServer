package com.sandy.capitalyst.server.core.nvpconfig;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import javax.persistence.PostPersist ;
import javax.persistence.PostUpdate ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPManager {
    
    private static final Logger log = Logger.getLogger( NVPManager.class ) ;

    private static NVPManager instance = null ;
    
    public static class NVPPersistCallback {
        
        @PostPersist
        @PostUpdate
        public void postNVPSave( NVP nvp ) {
            NVPManager.instance().notifyConfigChangeListeners( nvp ) ;
        }
    }
    
    public static NVPManager instance() {
        if( instance == null ) {
            instance = new NVPManager() ;
        }
        return instance ;
    }
    
    private NVPRepo nvpRepo = null ;
    private Map<String, Map<String, Set<NVPConfigChangeListener>>> listeners = null ;
    
    private NVPManager() {
        nvpRepo = CapitalystServer.getBean( NVPRepo.class ) ;
        listeners = new HashMap<>() ;
    }
    
    public NVPConfigGroup getConfigGroup( String groupName ) {
        
        NVPConfigGroup group = new NVPConfigGroup( groupName ) ;
        List<NVP> nvps = nvpRepo.findByGroupName( groupName ) ;
        for( NVP nvp : nvps ) {
            group.addNVPConfig( new NVPConfig( nvp ) ) ;
        }
        return group ;
    }
    
    public NVPConfig getConfig( String groupName, String keyName, 
                                String defaultValue ) {
        
        NVP nvp = nvpRepo.findByGroupNameAndConfigName( groupName, keyName ) ;
        
        if( nvp == null ) {
            nvp = new NVP( keyName, defaultValue ) ;
            nvp.setGroupName( groupName ) ;
            nvp = nvpRepo.save( nvp ) ;
        }
        return new NVPConfig( nvp ) ;
    }
    
    public void addConfigChangeListener( NVPConfigChangeListener listener,
                                         String groupName, 
                                         String... cfgKeys ) {
        
        if( cfgKeys != null && cfgKeys.length > 0 ) {
            for( String cfgKey : cfgKeys ) {
                addListener( groupName, cfgKey, listener ) ;
            }
        }
        else {
            List<NVP> nvps = nvpRepo.findByGroupName( groupName ) ;
            for( NVP nvp : nvps ) {
                addListener( groupName, nvp.getConfigName(), listener ) ;
            }
        }
    }
    
    private void addListener( String groupName, String cfgName,
                              NVPConfigChangeListener listener ) {
        
        Map<String, Set<NVPConfigChangeListener>> keyListenerMap = null ;
        
        keyListenerMap = listeners.get( groupName ) ;
        if( keyListenerMap == null ) {
            keyListenerMap = new HashMap<>() ;
            listeners.put( groupName, keyListenerMap ) ;
        }
        
        Set<NVPConfigChangeListener> listenerSet = null ;
        listenerSet = keyListenerMap.get( cfgName ) ;
        if( listenerSet == null ) {
            listenerSet = new HashSet<>() ;
            keyListenerMap.put( cfgName, listenerSet ) ;
        }
        
        if( !listenerSet.contains( listener ) ) {
            listenerSet.add( listener ) ;
        }
    }
    
    public void removeConfigChangeListener( NVPConfigChangeListener listener ) {
        
        listeners.values().forEach( keyListenerMap -> {
            keyListenerMap.values().forEach( listenerSet -> {
                listenerSet.remove( listener ) ;
            }) ;
        });
    }
    
    private void notifyConfigChangeListeners( NVP nvp ) {
        
        Set<NVPConfigChangeListener> listeners = getListeners( nvp ) ;
        if( listeners != null ) {
            listeners.forEach( listener -> {
                try {
                    NVPConfig cfg = new NVPConfig( nvp ) ;
                    listener.propertyChanged( cfg ) ;
                }
                catch( Exception e ) {
                    log.error( "Config change listener error", e ) ;
                }
            } ) ;
        }
    }
    
    private Set<NVPConfigChangeListener> getListeners( NVP nvp ) {
        
        Map<String, Set<NVPConfigChangeListener>> keyListenerMap = null ;
        
        keyListenerMap = listeners.get( nvp.getGroupName() ) ;
        if( keyListenerMap != null ) {
            
            Set<NVPConfigChangeListener> listenerSet = null ;
            
            listenerSet = keyListenerMap.get( nvp.getConfigName() ) ;
            if( listenerSet != null ) {
                return listenerSet ;
            }
        }
        
        return null ;
    }
}
