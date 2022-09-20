package com.sandy.capitalyst.server.dao.nvp;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPManager {

    public static NVPManager instance = null ;
    
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
    
    public NVP getNVP( String name ) {
        return nvpRepo.findByName( name ) ;
    }
    
    public void saveNVP( NVP nvp ) {
        this.nvpRepo.save( nvp ) ;
    }
}
