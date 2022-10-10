package com.sandy.capitalyst.server.api.equity.helper;

import java.util.Date ;

import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;

/**
 * One shot class. Imports the next iteration of historic data.
 * 
 * The server keeps historic EoD records for all the stocks whose 
 * indicators are being tracked. This list can grow/shrink over a period
 * based on intent.
 * 
 * The process of importing data is broken into chunks, such that for
 * each iteration (part import), only a certain range of eod data for a
 * particular stock is updated. This way, we spread the network access
 * over a period of time.
 * 
 * This class can be used both by the daemon and OTA.
 * 
 */
public class EquityHistDataPartImporter {
    
    public static final String CFG_GRP_NAME         = "EquityHistDataImporter" ;
    public static final String CFG_EOD_START_DATE   = "eod_bar_start_date" ;
    public static final String CFG_SCOOP_SIZE_DAYS  = "scoop_size_in_days" ;
    
    public static final String CFG_DEF_EOD_START_DATE  = "01-01-2014" ;
    public static final int    CFG_DEF_SCOOP_SIZE_DAYS = 365 ;
    
    private NVPConfigGroup cfg = null ;
    
    private Date earliestEodStartDate = null ;
    private int scoopSizeInDays = CFG_DEF_SCOOP_SIZE_DAYS ;
    
    public EquityHistDataPartImporter() {
        loadConfiguration() ;
    }
    
    private void loadConfiguration() {
        
        cfg = NVPManager.instance().getConfigGroup( CFG_GRP_NAME ) ;
        
        earliestEodStartDate = cfg.getDateValue( CFG_EOD_START_DATE, 
                                                 "01-01-2014" ) ;
        
        scoopSizeInDays = cfg.getIntValue( CFG_SCOOP_SIZE_DAYS, 
                                           CFG_DEF_SCOOP_SIZE_DAYS ) ;
    }

    public void execute() {
    }
}
