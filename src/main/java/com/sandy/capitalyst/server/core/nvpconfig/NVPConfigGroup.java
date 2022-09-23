package com.sandy.capitalyst.server.core.nvpconfig;

import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPConfigGroup {

    private String groupName = null ;
    private Map<String, NVPConfig> childCfgs = new HashMap<>() ;
    
    private NVPRepo nvpRepo = null ;
    
    public NVPConfigGroup( String groupName ) {
        this.groupName = groupName ;
        this.nvpRepo = CapitalystServer.getBean( NVPRepo.class ) ;
    }
    
    void addNVPConfig( NVPConfig cfg ) {
        this.childCfgs.put( cfg.getConfigName(), cfg ) ;
    }
    
    public String getGroupName() {
        return this.groupName ;
    }
    
    private NVPConfig getNVPConfig( String cfgKey, String defaultValue ) {
        NVPConfig cfg = this.childCfgs.get( cfgKey ) ;
        if( cfg == null ) {
            NVP nvp = new NVP( cfgKey, defaultValue ) ;
            nvp.setGroupName( groupName ) ;
            nvp = nvpRepo.save( nvp ) ;
            cfg = new NVPConfig( nvp ) ;
            this.childCfgs.put( cfgKey, cfg ) ;
        }
        
        return cfg ; 
    }
    
    public Integer getIntValue( String cfgKey, int defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, Integer.toString( defaultValue ) ) ;
        return cfg.getIntValue() ;
    }
    
    public Boolean getBooleanValue( String cfgKey, boolean defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, Boolean.toString( defaultValue ) ) ;
        return cfg.getBooleanValue() ;
    }
    
    public Date getDateValue( String cfgKey, Date defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, NVP.SDF.format( defaultValue ) ) ;
        return cfg.getDateValue() ;
    }
    
    public String[] getArrayValue( String cfgKey, String defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getArrayValue() ;
    }

    public void setValue( String cfgKey, int i ) {
        NVPConfig cfg = getNVPConfig( cfgKey, Integer.toString( i ) ) ;
        cfg.setValue( i ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, boolean b ) {
        NVPConfig cfg = getNVPConfig( cfgKey, Boolean.toString( b ) ) ;
        cfg.setValue( b ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, Date date ) {
        if( date == null ) {
            throw new IllegalArgumentException( "Cfg date value is null. " + 
                                                "Key = " + cfgKey ) ;
        }
        
        NVPConfig cfg = getNVPConfig( cfgKey, NVP.SDF.format( date ) ) ;
        cfg.setValue( date ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, String[] values ) {
        if( values == null ) {
            throw new IllegalArgumentException( "Cfg array value is null. " + 
                                                "Key = " + cfgKey ) ;
        }
        
        NVPConfig cfg = getNVPConfig( cfgKey, String.join( ",", values ) ) ;
        cfg.setValue( values ) ;
        cfg.save() ;
    }
}
