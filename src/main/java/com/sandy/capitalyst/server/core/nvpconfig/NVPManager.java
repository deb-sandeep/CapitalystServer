package com.sandy.capitalyst.server.core.nvpconfig;

import java.util.List ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPManager {

    private static NVPManager instance = null ;
    
    public static NVPManager instance() {
        if( instance == null ) {
            instance = new NVPManager() ;
        }
        return instance ;
    }
    
    private NVPRepo nvpRepo = null ;
    
    private NVPManager() {
        nvpRepo = CapitalystServer.getBean( NVPRepo.class ) ;
    }
    
    public NVPConfigGroup getConfigGroup( String groupName ) {
        
        NVPConfigGroup group = new NVPConfigGroup( groupName ) ;
        List<NVP> nvps = nvpRepo.findByGroup( groupName ) ;
        for( NVP nvp : nvps ) {
            group.addNVPConfig( new NVPConfig( nvp, nvpRepo ) ) ;
        }
        return group ;
    }
    
    public NVPConfig getConfig( String groupName, String keyName ) {
        
        NVP nvp = nvpRepo.findByGroupAndName( groupName, keyName ) ;
        if( nvp != null ) {
            return new NVPConfig( nvp, nvpRepo ) ;
        }
        return null ;
    }
}
