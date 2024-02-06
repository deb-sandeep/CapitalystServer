package com.sandy.capitalyst.server.api.equity.helper;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;
import static com.sandy.capitalyst.server.core.util.StringUtil.DD_MMM_YYYY ;

import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataImporter.ImportResult ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

/**
 * One shot class. Imports the next iteration of historic data.
 * <p>
 * The server keeps historic EoD records for all the stocks whose 
 * indicators are being tracked. This list can grow/shrink over a period
 * based on intent.
 * <p>
 * The process of importing data is broken into chunks, such that for
 * each iteration (part import), only a certain range of eod data for a
 * particular stock is updated. This way, we spread the network access
 * over a period of time.
 * <p>
 * This class can be used both by the daemon and OTA.
 * 
 */
public class EquityHistDataPartImporter {
    
    private static final Logger log = Logger.getLogger( EquityHistDataPartImporter.class ) ;

    // Why 60 days? The NSE API used to fetch historic data returns JSON
    // which is limited to a few rows only. For example, if we try fetching
    // EOD data from 1-1-2023 to 1-1-2024, we get results from 1-1-2023 till
    // say 22nd May. I believe there is a row limiting logic. So, we scoop
    // for lesser number of days to avoid introducing gaps in the history.
    public static final int MAX_SCOOP_SIZE_DAYS = 60;

    public static final String CFG_GRP_NAME         = "IndexHistDataImporter" ;
    public static final String CFG_EOD_START_DATE   = "eod_bar_start_date" ;
    public static final String CFG_SCOOP_SIZE_DAYS  = "scoop_size_in_days" ;
    public static final String CFG_IGNORE_SYMBOLS   = "ignore_symbols" ;

    private Date earliestEodStartDateLimit = null ;
    private int scoopSizeInDays = MAX_SCOOP_SIZE_DAYS ;
    private List<String> ignoreSymbols = null ;
    
    private EquityIndicatorsRepo eiRepo = null ;
    private HistoricEQDataMetaRepo eodMetaRepo = null ;
    private HistoricEQDataRepo eodRepo = null ;

    public EquityHistDataPartImporter() {
        loadRepositories() ;
    }
    
    private void loadRepositories() {
        
        eiRepo      = getBean( EquityIndicatorsRepo.class   ) ;
        eodRepo     = getBean( HistoricEQDataRepo.class ) ;     
        eodMetaRepo = getBean( HistoricEQDataMetaRepo.class ) ;
    }
    
    public ImportResult execute() throws Exception {
        
        log.debug( "" ) ;
        log.debug( "!- Executing EOD data import >" ) ;
        
        try {
            loadConfiguration() ;
            
            List<String> symbols = eiRepo.findSymbols() ;
            
            HistoricEQDataMeta meta = findSymbolForImport( symbols ) ;
            if( meta == null ) {
                log.info( "- All symbols have required historic data." ) ;
            }
            else {
                return importHistoricValues( meta ) ;
            } 
        }
        finally {
            log.debug( "<< Finished execution" ) ;
        }
        return null ;
    }

    private void loadConfiguration() {
        
        log.debug( "- Loading configuration" ) ;

        NVPConfigGroup cfg = NVPManager.instance().getConfigGroup(CFG_GRP_NAME);
        
        earliestEodStartDateLimit = cfg.getDateValue( CFG_EOD_START_DATE, 
                                                      "01-01-2014" ) ;
        
        scoopSizeInDays = cfg.getIntValue( CFG_SCOOP_SIZE_DAYS, 
                                           MAX_SCOOP_SIZE_DAYS ) ;

        if( scoopSizeInDays > MAX_SCOOP_SIZE_DAYS ) {
            scoopSizeInDays = MAX_SCOOP_SIZE_DAYS ;
            cfg.setValue( CFG_SCOOP_SIZE_DAYS, MAX_SCOOP_SIZE_DAYS ) ;
        }
        
        ignoreSymbols = cfg.getListValue( CFG_IGNORE_SYMBOLS, "" ) ;
        
        log.debug( "-> Earliest import date limit = " + 
                   DD_MMM_YYYY.format( earliestEodStartDateLimit ) ) ;
        log.debug( "-> Scoop size in days = " + scoopSizeInDays ) ;
    }

    // Finds the first symbol for which the earliest eod date has not reached
    // or for whom a meta record does not exist.
    private HistoricEQDataMeta findSymbolForImport( List<String> symbols ) {
        
        HistoricEQDataMeta meta = null ;
        
        for( String symbol : symbols ) {

            if( ignoreSymbols.contains( symbol ) ) {
                log.debug( "- Ignoring " + symbol + " as configured." ) ;
                continue ;
            }
            
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
                    // Boundary condition. Else ZYDUSLIFE keeps looping :)
                    meta = null ;
                }
            }
        }
        return meta ;
    }

    private ImportResult importHistoricValues( HistoricEQDataMeta meta ) 
        throws Exception {
        
        EquityHistDataImporter importer ;
        ImportResult result ;
        
        Date toDate = meta.getEarliestEodDate() ;
        Date fromDate ;
        
        String symbol = meta.getSymbolNse() ;
        
        toDate   = toDate == null ? new Date() : toDate ;
        fromDate = DateUtils.addDays( toDate, -scoopSizeInDays ) ;
        
        importer = new EquityHistDataImporter( eodRepo ) ;

        result = importer.importFromServer( symbol, fromDate, toDate ) ;
        
        if( result.isDataForWrongSymbolReceived() ) {
            // In effect, don't let the daemon process this symbol anymore
            meta.setEarliestEodDate( earliestEodStartDateLimit ) ;
        }
        else if( result.getNumRecordsFounds() == 0 ) {
            meta.setEarliestEodDate( earliestEodStartDateLimit ) ;
        }
        else {
            meta.setEarliestEodDate( fromDate ) ;
        }
        
        meta.setNumRecords( eodRepo.getNumRecords( meta.getSymbolNse() ) ) ;
        meta.setLastUpdate( new Date() ) ;
        
        eodMetaRepo.save( meta ) ;
        
        return result ;
    }
}
