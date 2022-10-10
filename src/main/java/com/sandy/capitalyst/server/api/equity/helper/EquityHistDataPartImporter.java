package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.core.util.StringUtil.DD_MMM_YYYY ;

import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataImporter.ImportResults ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;

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
    
    private static final Logger log = Logger.getLogger( EquityHistDataPartImporter.class ) ;
    
    public static final String CFG_GRP_NAME         = "EquityHistDataImporter" ;
    public static final String CFG_EOD_START_DATE   = "eod_bar_start_date" ;
    public static final String CFG_SCOOP_SIZE_DAYS  = "scoop_size_in_days" ;
    
    public static final String CFG_DEF_EOD_START_DATE  = "01-01-2014" ;
    public static final int    CFG_DEF_SCOOP_SIZE_DAYS = 365 ;
    
    private NVPConfigGroup cfg = null ;
    
    private Date earliestEodStartDateLimit = null ;
    private int scoopSizeInDays = CFG_DEF_SCOOP_SIZE_DAYS ;
    
    private EquityIndicatorsRepo eiRepo = null ;
    private HistoricEQDataMetaRepo eodMetaRepo = null ;
    
    public EquityHistDataPartImporter() {
        loadRepositories() ;
        loadConfiguration() ;
    }
    
    private void loadRepositories() {
        
        eiRepo      = getBean( EquityIndicatorsRepo.class   ) ;
        eodMetaRepo = getBean( HistoricEQDataMetaRepo.class ) ;
    }
    
    private void loadConfiguration() {
        
        log.debug( "- Loading configuration" ) ;
        
        cfg = NVPManager.instance().getConfigGroup( CFG_GRP_NAME ) ;
        
        earliestEodStartDateLimit = cfg.getDateValue( CFG_EOD_START_DATE, 
                                                 "01-01-2014" ) ;
        
        scoopSizeInDays = cfg.getIntValue( CFG_SCOOP_SIZE_DAYS, 
                                           CFG_DEF_SCOOP_SIZE_DAYS ) ;
        
        log.debug( "-> Earliest import date limit = " + DD_MMM_YYYY.format( earliestEodStartDateLimit ) ) ;
        log.debug( "-> Scoop size in days = " + scoopSizeInDays ) ;
    }

    public void execute() throws Exception {
        
        log.debug( "- Executing EOD data import" ) ;
        
        List<String> symbols = eiRepo.findSymbols() ;
        HistoricEQDataMeta meta = findSymbolForImport( symbols ) ;
        
        if( meta == null ) {
            log.info( "- All symbols have required historic data." ) ;
        }
        else {
            ImportResults result = importHistoricValues( meta ) ;
            
            log.info( "-> Num records found    = " + result.getNumRecordsFounds() ) ;
            log.info( "-> Num records imported = " + result.getNumAdditions() ) ;
            log.info( "-> Num dups deleted     = " + result.getNumDeletions() ) ;
        }
    }

    private HistoricEQDataMeta findSymbolForImport( List<String> symbols ) {
        
        HistoricEQDataMeta meta = null ;
        
        for( String symbol : symbols ) {
            
            meta = eodMetaRepo.findBySymbolNse( symbol ) ;
            
            if( meta == null ) {
                
                meta = new HistoricEQDataMeta() ;
                meta.setSymbolNse( symbol ) ;
                meta.setEarliestEodDate( null ) ;
                meta.setLastUpdate( null ) ;
                
                meta = eodMetaRepo.save( meta ) ;
                break ;
            }
            else {
                Date earliestImport = meta.getEarliestEodDate() ;
                if( earliestImport == null ) {
                    break ; 
                }
                else if( earliestImport.after( earliestEodStartDateLimit ) ) {
                    break ;
                }
                else {
                    meta = null ;
                }
            }
        }
        return meta ;
    }

    private ImportResults importHistoricValues( HistoricEQDataMeta meta ) 
        throws Exception {
        
        
        Date toDate = meta.getEarliestEodDate() == null ? 
                      new Date() : meta.getEarliestEodDate() ;
        Date fromDate = DateUtils.addDays( toDate, -scoopSizeInDays ) ;
        
        ImportResults result = null ;
        result = new EquityHistDataImporter( meta.getSymbolNse(), 
                                             fromDate, toDate ).execute() ;
        
        meta.setEarliestEodDate( fromDate ) ;
        meta.setLastUpdate( new Date() ) ;
        eodMetaRepo.save( meta ) ;
        
        return result ;
    }
}
