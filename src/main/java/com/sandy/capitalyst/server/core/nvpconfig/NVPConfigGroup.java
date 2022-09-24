package com.sandy.capitalyst.server.core.nvpconfig;

import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.validator.GenericValidator ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.dao.nvp.NVP ;
import com.sandy.capitalyst.server.dao.nvp.repo.NVPRepo ;

public class NVPConfigGroup {
    
    private static final Logger log = Logger.getLogger( NVPConfigGroup.class ) ;

    private String groupName = null ;
    private Map<String, NVPConfig> childCfgs = new HashMap<>() ;
    
    private static String SIMPLE_SDF_FMT = "dd-MM-yyyy" ;
    private static SimpleDateFormat SIMPLE_SDF = new SimpleDateFormat( SIMPLE_SDF_FMT ) ;
    
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
    
    public Boolean getBoolValue( String cfgKey, boolean defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, Boolean.toString( defaultValue ) ) ;
        return cfg.getBooleanValue() ;
    }
    
    public Date getDateValue( String cfgKey, Date defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, NVP.SDF.format( defaultValue ) ) ;
        return cfg.getDateValue() ;
    }
    
    public Date getDateValue( String cfgKey, String defaultValue ) {
        Date defaultDate = null ;
        try {
            if( GenericValidator.isDate( defaultValue, SIMPLE_SDF_FMT, true ) ) {
                defaultDate = SIMPLE_SDF.parse( defaultValue ) ;
            }
            else {
                defaultDate = NVP.SDF.parse( defaultValue ) ;
            }
        }
        catch( ParseException e ) {
            log.error( "Unparseable date " + defaultValue, e ) ;
        }
        return this.getDateValue( cfgKey, defaultDate ) ;
    }

    public String[] getArrayValue( String cfgKey, String defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getArrayValue() ;
    }

    public List<String> getListValue( String cfgKey, String defaultValue ) {
        NVPConfig cfg = getNVPConfig( cfgKey, defaultValue ) ;
        return cfg.getListValue() ;
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
