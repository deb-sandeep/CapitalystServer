package com.sandy.capitalyst.server.core.nvpconfig;

import java.util.Date ;

import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPConfig {
    
    private NVP nvp = null ;
    private NVPRepo nvpRepo = null ;
    
    NVPConfig( NVP nvp, NVPRepo nvpRepo ) {
        this.nvp = nvp ;
        this.nvpRepo = nvpRepo ;
    }
    
    public String getCfgKey() {
        return this.nvp.getName() ;
    }
    
    public String getDescription() {
        return this.nvp.getDescription() ;
    }
    
    public String getGroupName() {
        return this.nvp.getGroup() ;
    }

    public Integer getIntValue() {
        return nvp.getIntValue() ;
    }
    
    public Boolean getBooleanValue() {
        return nvp.getBooleanValue() ;
    }
    
    public Date getDateValue() {
        return nvp.getDateValue() ;
    }
    
    public String[] getArrayValue() {
        return nvp.getArrayValue() ;
    }
    
    public void setValue( Integer i ) {
        nvp.setValue( i ) ;
        save() ;
    }
    
    public void setValue( Boolean b ) {
        nvp.setValue( b ) ;
        save() ;
    }
    
    public void setValue( Date date ) {
        nvp.setValue( date ) ;
        save() ;
    }
    
    public void setValue( String[] values ) {
        nvp.setValue( values ) ;
        save() ;
    }
    
    void save() {
        this.nvpRepo.save( nvp ) ;
    }
}
