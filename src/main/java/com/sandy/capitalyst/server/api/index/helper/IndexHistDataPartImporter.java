package com.sandy.capitalyst.server.api.index.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.core.util.StringUtil.DD_MMM_YYYY ;

import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.index.helper.IndexHistDataImporter.ImportResult ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.index.HistoricIdxDataMeta ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.HistoricIdxDataMetaRepo ;
import com.sandy.capitalyst.server.dao.index.repo.HistoricIdxDataRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexMasterRepo ;

/**
 * One shot class. Imports the next iteration of historic data.
 * <p>
 * The server keeps historic EoD records for all the index whose 
 * indicators are being tracked. For the historic data of an index ot be 
 * tracked the histEnabled flag in the IndexMaster should be turned on.
 * <p>
 * The process of importing data is broken into chunks, such that for
 * each iteration (part import), only a certain range of eod data for a
 * particular index is updated. This way, we spread the network access
 * over a period of time.
 * <p>
 * This class can be used both by the daemon and OTA.
 */
public class IndexHistDataPartImporter {
    
    private static final Logger log = Logger.getLogger( IndexHistDataPartImporter.class ) ;
    
    public static final String CFG_GRP_NAME         = "IndexHistDataImporter" ;
    public static final String CFG_EOD_START_DATE   = "eod_bar_start_date" ;
    public static final String CFG_SCOOP_SIZE_DAYS  = "scoop_size_in_days" ;
    public static final String CFG_IGNORE_SYMBOLS   = "ignore_indexes" ;
    
    public static final int    CFG_DEF_SCOOP_SIZE_DAYS = 364 ;

    private Date earliestEodStartDateLimit = null ;
    private int scoopSizeInDays = CFG_DEF_SCOOP_SIZE_DAYS ;
    private List<String> ignoreIndexes = null ;
    
    private IndexMasterRepo         idxMasterRepo = null ;
    private HistoricIdxDataRepo     eodRepo       = null ;
    private HistoricIdxDataMetaRepo eodMetaRepo   = null ;
    
    public IndexHistDataPartImporter() {
        loadRepositories() ;
    }
    
    private void loadRepositories() {
        eodRepo       = getBean( HistoricIdxDataRepo.class ) ;     
        idxMasterRepo = getBean( IndexMasterRepo.class ) ;
        eodMetaRepo   = getBean( HistoricIdxDataMetaRepo.class ) ;
    }
    
    public void execute() throws Exception {
        
        List<IndexMaster> idxMasters = null ;
        HistoricIdxDataMeta meta = null ;
        
        log.debug( "" ) ;
        log.debug( "!- Executing Index EOD data import >" ) ;
        
        try {
            loadConfiguration() ;
            
            idxMasters = idxMasterRepo.findEodEnabledIndexes() ;
            meta = findIndexForImport( idxMasters ) ;
            
            if( meta == null ) {
                log.info( "- All symbols have required historic data." ) ;
            }
            else {
                importHistoricValues( meta ) ;
            } 
        }
        finally {
            log.debug( "<< Finished execution" ) ;
        }
    }

    private void loadConfiguration() {
        
        log.debug( "- Loading configuration" ) ;

        NVPConfigGroup cfg = NVPManager.instance().getConfigGroup(CFG_GRP_NAME);
        
        earliestEodStartDateLimit = cfg.getDateValue( CFG_EOD_START_DATE, 
                                                 "01-01-2014" ) ;
        
        scoopSizeInDays = cfg.getIntValue( CFG_SCOOP_SIZE_DAYS, 
                                           CFG_DEF_SCOOP_SIZE_DAYS ) ;
        
        ignoreIndexes = cfg.getListValue( CFG_IGNORE_SYMBOLS, "" ) ;
        
        log.debug( "-> Earliest import date limit = " + 
                   DD_MMM_YYYY.format( earliestEodStartDateLimit ) ) ;
        log.debug( "-> Scoop size in days = " + scoopSizeInDays ) ;
    }

    // Finds the first index for which the earliest eod date has not reached
    // or for whom a meta record does not exist.
    private HistoricIdxDataMeta findIndexForImport( List<IndexMaster> idxMasters ) {
        
        HistoricIdxDataMeta meta = null ;
        
        for( IndexMaster idxMaster : idxMasters ) {

            if( ignoreIndexes.contains( idxMaster.getName() ) ) {
                log.debug( "- Ignoring " + idxMaster.getName() + " as configured." ) ;
                continue ;
            }
            
            meta = eodMetaRepo.findByIndex( idxMaster ) ;
            if( meta == null ) {
                
                meta = new HistoricIdxDataMeta() ;
                meta.setIndex( idxMaster ) ;
                meta.setIndexName( idxMaster.getName() ) ;
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

    private ImportResult importHistoricValues( HistoricIdxDataMeta meta ) 
        throws Exception {
        
        IndexHistDataImporter importer ;
        ImportResult result ;
        
        Date toDate = meta.getEarliestEodDate() ;
        Date fromDate ;
        
        toDate   = toDate == null ? new Date() : toDate ;
        fromDate = DateUtils.addDays( toDate, -scoopSizeInDays ) ;
        
        importer = new IndexHistDataImporter( meta.getIndex(), fromDate, toDate ) ;

        result = importer.execute() ;
        
        if( result.getNumRecordsFounds() == 0 ) {
            meta.setEarliestEodDate( earliestEodStartDateLimit ) ;
        }
        else {
            meta.setEarliestEodDate( fromDate ) ;
        }
        
        meta.setNumRecords( eodRepo.getNumRecords( meta.getIndex() ) ) ;
        meta.setLastUpdate( new Date() ) ;
        
        eodMetaRepo.save( meta ) ;
        
        return result ;
    }
}
