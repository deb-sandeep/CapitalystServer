package com.sandy.capitalyst.server.core.nvpconfig;

import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

public class NVPConfigGroup {

    private String groupName = null ;
    private Map<String, NVPConfig> childCfgs = new HashMap<>() ;
    
    public NVPConfigGroup( String groupName ) {
        this.groupName = groupName ;
    }
    
    void addNVPConfig( NVPConfig cfg ) {
        this.childCfgs.put( cfg.getCfgKey(), cfg ) ;
    }
    
    public String getGroupName() {
        return this.groupName ;
    }
    
    private NVPConfig getNVPConfig( String cfgKey ) {
        return this.childCfgs.get( cfgKey ) ;
    }
    
    public Integer getIntValue( String cfgKey ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        return cfg.getIntValue() ;
    }
    
    public Boolean getBooleanValue( String cfgKey ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        return cfg.getBooleanValue() ;
    }
    
    public Date getDateValue( String cfgKey ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        return cfg.getDateValue() ;
    }
    
    public String[] getArrayValue( String cfgKey ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        return cfg.getArrayValue() ;
    }

    public void setValue( String cfgKey, Integer i ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        cfg.setValue( i ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, Boolean b ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        cfg.setValue( b ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, Date date ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        cfg.setValue( date ) ;
        cfg.save() ;
    }
    
    public void setValue( String cfgKey, String[] values ) {
        NVPConfig cfg = getNVPConfig( cfgKey ) ;
        cfg.setValue( values ) ;
        cfg.save() ;
    }
}
