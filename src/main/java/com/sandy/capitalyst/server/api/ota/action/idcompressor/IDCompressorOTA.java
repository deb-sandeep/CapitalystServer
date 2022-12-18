package com.sandy.capitalyst.server.api.ota.action.idcompressor;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.List ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.EntityWithNumericID ;
import com.sandy.capitalyst.server.dao.IDCompressor ;
import com.sandy.capitalyst.server.dao.idgen.IDGen ;
import com.sandy.capitalyst.server.dao.idgen.repo.IDGenRepo ;

public class IDCompressorOTA extends OTA {
    
    static final Logger log = Logger.getLogger( IDCompressorOTA.class ) ;
    
    public static final String NAME = "IDCompressor" ;
    
    public static final String CFG_GRP_NAME = NAME ;
    
    public static final String CFG_TABLE_NAME   = "table_name" ;
    public static final String CFG_NEXT_ID_VAL  = "next_id_val" ;
    public static final String CFG_BATCH_SIZE   = "batch_size" ;
    public static final String CFG_BATCH_OFFSET = "batch_offset" ;
    public static final String CFG_NUM_ITERS    = "num_iters_per_run" ;
    public static final String CFG_ITER_SLEEP   = "sleep_interval_millis" ;
    
    private class Config {
        public String tableName ;
        public int    nextIdVal ;
        public int    batchSize ;
        public int    batchOffset ;
        public int    numItersPerRun ;
        public int    sleepInterval ;
    }
    
    private Config config = new Config() ;
    
    public IDCompressorOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        loadConfig( true ) ;
        if( StringUtil.isEmptyOrNull( config.tableName ) ) {
            
            log.info( "ERROR : table_name or id_gen_key not configured." ) ;
        }
        else {
            int totalRecsCompressed = 0 ;
            for( int i=0; i<config.numItersPerRun; i++ ) {
                
                log.info( "Executing iteration " + i ) ;
                int numRecsCompressed = 0 ;
                
                switch( config.tableName ) {
                    /*
                    case "historic_eq_data":
                        numRecsCompressed = compressBatch( HistoricEQDataRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "historic_idx_data":
                        numRecsCompressed = compressBatch( HistoricIdxDataRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "equity_indicators_hist":
                        numRecsCompressed = compressBatch( EquityIndicatorsHistRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "breeze_invocation_stats":
                        numRecsCompressed = compressBatch( BreezeInvocationStatsRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "equity_tech_indicator":
                        numRecsCompressed = compressBatch( EquityTechIndicatorRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "historic_mf_data":
                        numRecsCompressed = compressBatch( HistoricMFDataRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "equity_daily_gain":
                        numRecsCompressed = compressBatch( EquityDailyGainRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "job_run":
                        numRecsCompressed = compressBatch( JobRunEntryRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;

                    case "equity_trade":
                        numRecsCompressed = compressBatch( EquityTradeRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    case "equity_txn":
                        numRecsCompressed = compressBatch( EquityTxnRepo.class,
                                                           config.tableName + "_id" ) ;
                        break ;
                        
                    */
                        
                    default:
                        log.info( "ERROR: Unrecognized table name." ) ;
                }
                
                if( numRecsCompressed == 0 ) {
                    break ;
                }
                else {
                    totalRecsCompressed += numRecsCompressed ;
                }
                log.info( "-> " + totalRecsCompressed + " total records compressed." ) ;
                
                loadConfig( false ) ;
                TimeUnit.MILLISECONDS.sleep( config.sleepInterval ) ;
            }
        }
    }
    
    private void loadConfig( boolean printValues ) {
        
        NVPConfigGroup cfg = NVPManager.instance()
                                       .getConfigGroup( CFG_GRP_NAME ) ;
        
        config.tableName      = cfg.getStringValue( CFG_TABLE_NAME,   "historic_eq_data" ) ;
        config.nextIdVal      = cfg.getIntValue   ( CFG_NEXT_ID_VAL,  1  ) ;
        config.batchSize      = cfg.getIntValue   ( CFG_BATCH_SIZE,   100 ) ;
        config.batchOffset    = cfg.getIntValue   ( CFG_BATCH_OFFSET, 0  ) ;
        config.numItersPerRun = cfg.getIntValue   ( CFG_NUM_ITERS,    5  ) ;
        config.sleepInterval  = cfg.getIntValue   ( CFG_ITER_SLEEP,   500  ) ;
        
        if( printValues ) {
            log.info( "Config values:" ) ;
            log.info( "    Table name       - " + config.tableName      ) ;
            log.info( "    Next ID val      - " + config.nextIdVal      ) ;
            log.info( "    Batch size       - " + config.batchSize      ) ;
            log.info( "    Batch offset     - " + config.batchOffset    ) ;
            log.info( "    Num iter per run - " + config.numItersPerRun ) ;
            log.info( "    Sleep interval   - " + config.sleepInterval  ) ;
        }
    }

    private void saveConfig() {
        
        log.debug( "Saving config with new batch offset and next id val" ) ;

        NVPConfigGroup cfg = NVPManager.instance()
                                       .getConfigGroup( CFG_GRP_NAME ) ;
        
        cfg.setValue( CFG_NEXT_ID_VAL, config.nextIdVal ) ;
        cfg.setValue( CFG_BATCH_OFFSET, config.batchOffset );
    }
    
    int compressBatch( Class<? extends IDCompressor> compressorRepoClass,
                               String idGenKey ) {
        
        IDCompressor repo = null ;
        EntityWithNumericID entity = null ;
        List<? extends Object> batch = null ;
        
        repo = getBean( compressorRepoClass ) ;
        batch = (( IDCompressor )repo).getBatchOfRecords( config.batchOffset, config.batchSize ) ;

        if( batch == null || batch.isEmpty() ) {
            
            log.info( "Table " + config.tableName + " compressed." ) ;
            log.debug( "Updating ID gen value" ) ;
            
            updateIDGenValue( idGenKey ) ;
            return 0 ;
        }
        
        int nextId = config.nextIdVal ;
        int nextOffset = config.batchOffset + batch.size() ;
        
        for( Object record : batch ) {
            
            entity = ( EntityWithNumericID )record ;
            repo.changeID( entity.getId(), nextId ) ;
            nextId++ ;
        }
        
        config.nextIdVal = nextId ;
        config.batchOffset = nextOffset ;
        saveConfig() ;

        return batch.size() ;
    }
    
    private void updateIDGenValue( String idGenKey ) {
        
        IDGenRepo repo = getBean( IDGenRepo.class ) ;
        IDGen record = repo.findByGenKey( idGenKey ) ;
        
        if( record == null ) {
            record = new IDGen() ;
            record.setGenKey( idGenKey ) ;
        }
        
        record.setGenValue( config.nextIdVal ) ;
        repo.save( record ) ;
    }
}
