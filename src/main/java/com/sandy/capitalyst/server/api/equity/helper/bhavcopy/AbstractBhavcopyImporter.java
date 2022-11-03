package com.sandy.capitalyst.server.api.equity.helper.bhavcopy;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityTTMPerfUpdater ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQDataMeta ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataMetaRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.dao.index.repo.IndexEquityRepo ;
import com.sandy.capitalyst.server.job.equity.eodrefresh.EquityDailyGainUpdater ;
import com.sandy.common.util.StringUtil ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

import static com.sandy.capitalyst.server.core.util.StringUtil.DD_MM_YYYY ;

public abstract class AbstractBhavcopyImporter {
    
    private static final Logger log = Logger.getLogger( AbstractBhavcopyImporter.class ) ;

    private static final String NIFTY_200_IDX_NAME = "Nifty 200" ;
    
    private HistoricEQDataRepo     ecRepo  = null ;
    private HistoricEQDataMetaRepo ecmRepo = null ;
    private EquityMasterRepo       emRepo  = null ;
    private EquityHoldingRepo      ehRepo  = null ;
    private IndexEquityRepo        ieRepo  = null ;

    private EquityDailyGainUpdater dgUpdater = null ;
    
    private List<String> nifty200Stocks = new ArrayList<>() ;
    
    public AbstractBhavcopyImporter() {
        
        ecRepo  = getBean( HistoricEQDataRepo.class     ) ;
        ecmRepo = getBean( HistoricEQDataMetaRepo.class ) ;
        emRepo  = getBean( EquityMasterRepo.class       ) ;
        ehRepo  = getBean( EquityHoldingRepo.class      ) ;
        ieRepo  = getBean( IndexEquityRepo.class        ) ;
        
        dgUpdater = new EquityDailyGainUpdater() ;
        
        nifty200Stocks.addAll( ieRepo.findEquitiesForIndex( NIFTY_200_IDX_NAME ) ) ;
    }

    public BhavcopyImportResult importContents( String content ) 
        throws Exception {
        
        Date                 bhavcopyDate = null ;
        List<String[]>       csvData      = null ;
        CsvParserSettings    settings     = null ;
        EquityTTMPerfUpdater ttmUpdater   = null ;
        StringReader         contentReader= null ;
        BhavcopyImportResult results      = null ;
        HistoricEQData       candle       = null ;
        Date                 latestRecDt  = null ;
        boolean              isLatest     = false ;
        
        Map<String, List<EquityHolding>> holdingsMap = null ;
        
        holdingsMap   = loadHoldingsMap() ;
        ttmUpdater    = new EquityTTMPerfUpdater() ;
        settings      = new CsvParserSettings() ;
        contentReader = new StringReader( content ) ;
        
        settings.detectFormatAutomatically() ;
        
        log.debug( "- Parsing CSV contents" ) ;
        csvData = new CsvParser( settings ).parseAll( contentReader ) ;
        
        results = new BhavcopyImportResult() ;
        results.setNumRecordsFound( csvData.size()-1 ) ;
        
        for( int i=1;i<csvData.size(); i++ ) {
            
            String[] record = csvData.get( i ) ;
            
            if( !shouldProcessRecord( record ) ) {
                continue ;
            }
            
            EquityMaster em = getEquityMaster( record ) ;
            if( em == null ) {
                continue ;
            }
            
            if( bhavcopyDate == null ) {
                
                bhavcopyDate = getBhavcopyDate( record ) ;
                results.setBhavcopyDate( bhavcopyDate ) ;
                
                log.debug( "- Bhavcopy date detected " + 
                           DD_MM_YYYY.format( bhavcopyDate ) );
                
                latestRecDt = ecRepo.findLatestRecordDate() ;
                if( latestRecDt.before( bhavcopyDate ) ) {
                    
                    log.debug( "-> Bhavcopy is the latest." ) ;
                    isLatest = true ;
                }
            }
            
            log.debug( "-> Updating " + em.getSymbol() ) ;
            candle = buildEquityCandle( record, bhavcopyDate ) ;
            
            em.setClose( candle.getClose() ) ;
            em.setPrevClose( candle.getPrevClose() ) ;
            
            emRepo.saveAndFlush( em ) ;
            
            if( StringUtil.isNotEmptyOrNull( em.getIndustry() ) || 
                em.isEtf()                                      || 
                holdingsMap.containsKey( em.getSymbol() )       || 
                nifty200Stocks.contains( em.getSymbol() ) ) {
                
                ecRepo.saveAndFlush( candle ) ;
                results.incNumRecordsImported();
                
                updateEquityHistMeta( em.getSymbol() ) ;
                
                if( isLatest ) {
                    updateEquityISINMapping( em.getSymbol(), em.getIsin() ) ;
                    ttmUpdater.addTodayEODCandle( candle ) ;
                }
            }
            
            if( isLatest && holdingsMap.containsKey( em.getSymbol() ) ) {
                
                for( EquityHolding holding : holdingsMap.get( em.getSymbol() ) ) {
                    
                    if( holding.getLastUpdate().before( bhavcopyDate ) ) {
                        
                        log.debug( "-> Updating daily gain. Holding " + holding.getId() ) ;
                        dgUpdater.updateEDG( holding, candle ) ;
                        
                        holding.setCurrentMktPrice( candle.getClose() ) ;
                        holding.setLastUpdate( new Date() ) ;
                        ehRepo.saveAndFlush( holding ) ;
                    }
                }
            }
        }
        
        if( isLatest ) {
            ttmUpdater.updateTTMPerfMeasures() ;
        }
        
        return results ;
    }
    
    protected abstract Date getBhavcopyDate( String[] record ) throws Exception ;
    
    protected abstract boolean shouldProcessRecord( String[] record ) ;
    
    protected abstract EquityMaster getEquityMaster( String[] record ) ;
    
    protected abstract HistoricEQData buildEquityCandle( String[] record, Date date ) ;

    private Map<String, List<EquityHolding>> loadHoldingsMap() {
        
        log.debug( "- Loading holdings map." ) ;
        
        List<EquityHolding> holdings = ehRepo.findNonZeroHoldings() ;
        Map<String, List<EquityHolding>> holdingsMap = new HashMap<>() ;
        
        for( EquityHolding holding : holdings ) {
            String symbol = holding.getSymbolNse() ;
            if( StringUtil.isNotEmptyOrNull( symbol ) ) {
                List<EquityHolding> holdingList = holdingsMap.get( symbol ) ;
                if( holdingList == null ) {
                    holdingList = new ArrayList<>() ;
                    holdingsMap.put( symbol, holdingList ) ;
                }
                holdingList.add( holding ) ;
            }
        }
        return holdingsMap ;
    }

    private void updateEquityISINMapping( String symbol, String isin ) {
        
        try {
            EquityMaster eqIsin = emRepo.findByIsin( isin ) ;
            if( eqIsin == null ) {
                // If we can't find a mapping via ISIN, it can also mean than
                // the ISIN has changed. In this case, we see if the we can 
                // find a mapping via the symbol before trying to create a 
                // new mapping.
                //
                // Case observed - IRCON ISIN changed on 7th May 2020
                eqIsin = emRepo.findBySymbol( symbol ) ;
                if( eqIsin != null ) {
                    eqIsin.setIsin( isin ) ;
                    
                    // In case we have updated an existing mapping, we have to
                    // check if this ISIN was being used by any of our
                    // existing holdings. If so, we update those too.
                    ehRepo.updateISIN( isin, symbol ) ;
                }
                else {
                    eqIsin = new EquityMaster() ;
                    eqIsin.setSymbol( symbol ) ;
                    eqIsin.setIsin( isin ) ;
                }
                emRepo.save( eqIsin ) ;
            }
        }
        catch( Exception e ) {
            log.error( "Could not update equity ISIN mapping.", e ) ; 
            log.error( "\tSymbol = " + symbol ) ;
            log.error( "\tISIN = " + isin ) ;
            throw e ;
        }
    }
    
    private void updateEquityHistMeta( String symbol ) {
        
        HistoricEQDataMeta meta = ecmRepo.findBySymbolNse( symbol ) ;
        if( meta != null ) {
            
            meta.setNumRecords( ecRepo.getNumRecords( symbol ) ) ;
            meta.setLastUpdate( new Date() ) ;
            
            ecmRepo.saveAndFlush( meta ) ;
        }
    }
}
