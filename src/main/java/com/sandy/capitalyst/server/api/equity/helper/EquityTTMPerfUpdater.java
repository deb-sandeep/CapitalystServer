package com.sandy.capitalyst.server.api.equity.helper;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager;
import com.sandy.capitalyst.server.core.util.StringUtil;
import com.sandy.capitalyst.server.daemon.equity.recoengine.RecoManager;
import com.sandy.capitalyst.server.dao.equity.EquityTTMPerf;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTTMPerfRepo;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo.ClosePrice;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sandy.capitalyst.server.CapitalystServer.getBean;

public class EquityTTMPerfUpdater {
    
    private static final Logger log = Logger.getLogger( EquityTTMPerfUpdater.class ) ;
    
    public static final String CFG_GRP_NAME    = "EquityPerfTTMUpdater" ;
    public static final String CFG_INCL_STOCKS = "incl_stocks" ;
    public static final String CFG_EXCL_STOCKS = "excl_stocks" ;
    
    private static final String LAST_24M_EOD_URL = 
        "https://www1.nseindia.com/" + 
        "products/dynaContent/common/productsSymbolMapping.jsp?" + 
        "symbol={symbol}&" + 
        "segmentLink=3&" + 
        "symbolCount=1&" + 
        "series=EQ&" + 
        "dateRange=24month&" + 
        "fromDate=&toDate=&" + 
        "dataType=PRICEVOLUME" ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    static class Config {
        List<String> inclStocks = null ;
        List<String> exclStocks = null ;
    }

    private final Map<String, HistoricEQData> todayCandles = new HashMap<>() ;
    private final Map<String, EquityTTMPerf>  perfMap      = new HashMap<>() ;
    
    private final HistoricEQDataRepo histRepo ;
    private final EquityTTMPerfRepo  perfRepo ;
    
    private Object[][] ttmTimeMarkers = null ;
    
    private final Config cfg ;

    public EquityTTMPerfUpdater() {
        
        cfg = loadConfig() ;

        histRepo = getBean( HistoricEQDataRepo.class ) ;
        perfRepo = getBean( EquityTTMPerfRepo.class ) ;
    }
    
    private Config loadConfig() { 
        
        log.debug( "- Loading config" ) ;
        
        NVPConfigGroup nvpCfg = NVPManager.instance()
                                          .getConfigGroup( CFG_GRP_NAME ) ;
        Config cfg = new Config();
        cfg.inclStocks = nvpCfg.getListValue( CFG_INCL_STOCKS, "" ) ;
        cfg.exclStocks = nvpCfg.getListValue( CFG_EXCL_STOCKS, "" ) ;
        
        log.debug( "-> Included stocks = " + String.join( ",", cfg.inclStocks ) ) ;
        log.debug( "-> Excluded stocks = " + String.join( ",", cfg.exclStocks ) ) ;
        
        return cfg ;
    }
    
    private Date getFYStartDate() {
        
        Calendar cal    = Calendar.getInstance() ;
        int      fyYear = cal.get( Calendar.YEAR ) ;
        int      month  = cal.get( Calendar.MONTH ) ;
        
        if( month <= Calendar.MARCH ) {
            fyYear -= 1 ;
        }
        cal.set( fyYear, Calendar.APRIL, 1, 0, 0, 0 ) ;
        return cal.getTime() ;
    }
    
    public void addTodayEODCandle( HistoricEQData c ) {
        
        if( shouldProcessSymbol( c.getSymbol() ) ) {
            todayCandles.put( c.getSymbol(), c ) ;
        }
        else {
            log.debug( "-> Filtering " + c.getSymbol() ) ;
        }
    }
    
    private boolean shouldProcessSymbol( String symbol ) {
        
        // If no include stocks are specified, we include all, else any 
        // stock not in the include list is ignored.
        if( !cfg.inclStocks.isEmpty() ) {
            if( !cfg.inclStocks.contains( symbol ) ) {
                return false ;
            }
        }
        
        // If no exclude stocks are specified, we include all, else any
        // stock in the exclude stock is rejected
        if( !cfg.exclStocks.isEmpty() ) {
            return !cfg.exclStocks.contains( symbol );
        }
        
        return true ;
    }

    public int getNumStocksForUpdate() {
        return todayCandles.size() ;
    }

    public void updateTTMPerfMeasures() throws Exception { 
        
        log.debug( "- Updating TTM perf" ) ;
        log.debug( "-> Num stocks = " + getNumStocksForUpdate() + " !>" ) ;
        
        generateTTMDateMarkers() ;
        preloadTTMPerfRecords() ;
        
        for( Object[] meta : ttmTimeMarkers ) {
            
            String milestone = ( String )meta[0] ;
            Date   date      = ( Date   )meta[1] ;
            
            updateMilestonePerf( milestone, date ) ;
        }

        log.debug( "- Saving all perf records." ) ;
        
        int   numRecords   = perfMap.size() ;
        int   curRecord    = -1 ;
        float pctCompleted ;

        for( EquityTTMPerf record : perfMap.values() ) {
            perfRepo.saveAndFlush( record );

            curRecord++;

            if( curRecord % 50 == 0 ) {
                pctCompleted = ((( float ) curRecord) / numRecords) * 100;
                log.debug( "->> " + Math.ceil( pctCompleted ) + "% completed." );
            }
        }
        
        RecoManager.instance().setEquityDataUpdated( true ) ;
        
        log.debug( "<< TTM perf refresh completed." ) ;
    }
    
    private void generateTTMDateMarkers() {
        
        Date today = todayCandles.entrySet().iterator().next().getValue().getDate() ;
        
        Date back1D  = DateUtils.addDays  ( today, -1  ) ;
        Date back1W  = DateUtils.addDays  ( today, -7  ) ;
        Date back2W  = DateUtils.addDays  ( today, -14 ) ;
        Date back1M  = DateUtils.addMonths( today, -1  ) ;
        Date back2M  = DateUtils.addMonths( today, -2  ) ;
        Date back3M  = DateUtils.addMonths( today, -3  ) ;
        Date back4M  = DateUtils.addMonths( today, -4  ) ;
        Date back5M  = DateUtils.addMonths( today, -5  ) ;
        Date back6M  = DateUtils.addMonths( today, -6  ) ;
        Date back7M  = DateUtils.addMonths( today, -7  ) ;
        Date back8M  = DateUtils.addMonths( today, -8  ) ;
        Date back9M  = DateUtils.addMonths( today, -9  ) ;
        Date back10M = DateUtils.addMonths( today, -10 ) ;
        Date back11M = DateUtils.addMonths( today, -11 ) ;
        Date back12M = DateUtils.addMonths( today, -12 ) ;
        Date fyStart = getFYStartDate() ;
        
        ttmTimeMarkers     = new Object[16][] ;
        ttmTimeMarkers[ 0] = new Object[]{ "perf1d" , back1D  } ;     
        ttmTimeMarkers[ 1] = new Object[]{ "perf1w" , back1W  } ;     
        ttmTimeMarkers[ 2] = new Object[]{ "perf2w" , back2W  } ;     
        ttmTimeMarkers[ 3] = new Object[]{ "perf1m" , back1M  } ;     
        ttmTimeMarkers[ 4] = new Object[]{ "perf2m" , back2M  } ;     
        ttmTimeMarkers[ 5] = new Object[]{ "perf3m" , back3M  } ;     
        ttmTimeMarkers[ 6] = new Object[]{ "perf4m" , back4M  } ;     
        ttmTimeMarkers[ 7] = new Object[]{ "perf5m" , back5M  } ;     
        ttmTimeMarkers[ 8] = new Object[]{ "perf6m" , back6M  } ;     
        ttmTimeMarkers[ 9] = new Object[]{ "perf7m" , back7M  } ;     
        ttmTimeMarkers[10] = new Object[]{ "perf8m" , back8M  } ;     
        ttmTimeMarkers[11] = new Object[]{ "perf9m" , back9M  } ;     
        ttmTimeMarkers[12] = new Object[]{ "perf10m", back10M } ;     
        ttmTimeMarkers[13] = new Object[]{ "perf11m", back11M } ;     
        ttmTimeMarkers[14] = new Object[]{ "perf12m", back12M } ;     
        ttmTimeMarkers[15] = new Object[]{ "perfFy" , fyStart } ;     
    }
    
    private void preloadTTMPerfRecords() {
        
        log.debug( "- Preloading TTM perf records" ) ;
        
        for( HistoricEQData currEod : todayCandles.values() ) {
            
            String symbol = currEod.getSymbol() ;
            
            EquityTTMPerf perf = perfRepo.findBySymbolNse( symbol ) ;
            if( perf == null ) {
                log.debug( "->> TTM record not found. Creating new." ) ;
                perf = new EquityTTMPerf() ;
                perf.setSymbolNse( currEod.getSymbol() ) ;
            }
            perf.setCurrentPrice( currEod.getClose() ) ;
            
            perfMap.put( currEod.getSymbol(), perf ) ;
        }
    }
    
    private void updateMilestonePerf( String perfField, Date date ) 
        throws Exception {
        
        log.debug( "- Updating TTM " + perfField + " @ " + SDF.format( date ) ) ;
     
        List<ClosePrice>        histEODList ;
        Map<String, ClosePrice> histEODMap  = new HashMap<>() ;
        
        histEODList = histRepo.getClosePriceNearestToDate( date ) ;
        histEODList.forEach( i -> histEODMap.put( i.getSymbol(), i ) ) ;
        
        for( HistoricEQData candle : todayCandles.values() ) {
            
            String        symbol = candle.getSymbol() ;
            EquityTTMPerf perf   = perfMap.get( symbol ) ;
            
            ClosePrice histCP = histEODMap.get( symbol ) ;
            
            if( histCP == null ) {
                // If we don't have a historic eod price it can be because of the
                // following reasons:
                //
                // 1. This is the first time we are creating perf stats for this stock
                // 2. Historic EOD data for this stock is missing
                // 3. There is a genuine gap in EOD data
                //
                // We try to fill the gap if possible
                if( perf.genuineGapExists() ) {
                    
                    log.debug( "!> Genuine gap found for " + symbol ) ;

                    try {
                        final HistoricEQData milestoneCP = fillGapsInHistoricData( perf, date ) ;
                        if( milestoneCP != null ) {

                            histCP = new ClosePrice() {
                                public Date getDate() {
                                    return milestoneCP.getDate() ;
                                }

                                public String getSymbol() {
                                    return milestoneCP.getSymbol() ;
                                }

                                public float getClose() {
                                    return milestoneCP.getClose() ;
                                }
                            } ;
                        }
                        log.debug( "- Gap filled <<" ) ;
                    }
                    catch( Exception e ) {
                        // Do not propagate the error. We can't have the bhavcopy
                        // import failing just because historic data of one symbol
                        // is not found.
                        log.error( "Could not fill gap in historic data for " +
                                   perf.getSymbolNse() + " and date " + date, e ) ;
                    }
                }
            }
            
            if( histCP != null ) {
                populatePerf( histCP, perfField, perf ) ;
            }
        }
    }

    private void populatePerf( ClosePrice histCP,
                               String field, EquityTTMPerf perf ) 
        throws Exception {
        
        float pricePerf = (( perf.getCurrentPrice() - histCP.getClose() )/
                             histCP.getClose() )*100 ;
        
        BeanUtils.setProperty( perf, field, pricePerf ) ;
    }
    
    // Fills any EOD gaps with data downloaded from the exchange. Returns
    // a closing price closest in past to the milestone date specified.
    private HistoricEQData fillGapsInHistoricData( EquityTTMPerf perf, 
                                                   Date milestoneDate ) 
        throws Exception {
        
        String symbol = perf.getSymbolNse() ;
        
        log.debug( "> Filling historic gap for " + symbol ) ;
        
        String DIV_START = "<div id='csvContentDiv' style='display:none;'>" ;
        String url = LAST_24M_EOD_URL.replace( "{symbol}", symbol ) ;
        
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        String response ;
        String csvContent = null ;
        HistoricEQData eodDataClosestToMilestone = null ;
        
        response = downloader.getResource( url, "nse-bhav.txt" ) ;
        
        int startIndex = response.indexOf( DIV_START ) ;
        if( startIndex != -1 ) {
            startIndex += DIV_START.length() ;
            int endIndex = response.indexOf( "</div>", startIndex ) ;
            csvContent = response.substring( startIndex, endIndex ) ;
        }
        
        if( StringUtil.isNotEmptyOrNull( csvContent ) ) {
            
            CsvParserSettings settings = new CsvParserSettings() ;
            CsvParser parser = new CsvParser( settings ) ;
            List<String[]> records ;
            
            csvContent = csvContent.replace( ":", "\n" ) ;
            records = parser.parseAll( new StringReader( csvContent ) ) ;
            
            // Row 0 is the header row. Ignore it.
            int numGapsFilled = 0 ;
            
            for( int i=1; i<records.size(); i++ ) {
                String[] row = records.get( i ) ;
                
                HistoricEQData eodData = addHistoricRecord( row ) ;
                
                if( eodData != null ) {
                    numGapsFilled++ ;
                    if( eodDataClosestToMilestone == null ) {
                        if( eodData.getDate().before( milestoneDate ) ) {
                            eodDataClosestToMilestone = eodData ;
                        }
                    }
                }
            }
            
            log.debug( "-> " + numGapsFilled + " gaps filled." ) ;
        }
        
        perf.setGapsFilled( true ) ;
        perfRepo.saveAndFlush( perf ) ;
        
        return eodDataClosestToMilestone ;
    }
    
    private HistoricEQData addHistoricRecord( String[] row ) throws Exception {
        
        HistoricEQData eodData = null ;
        List<HistoricEQData> histRows ;
        
        String symbol        = row[0].trim() ;
        Date   date          = SDF.parse       ( row[ 2].trim() ) ;
        float  open          = Float.parseFloat( row[ 4].trim() ) ;
        float  high          = Float.parseFloat( row[ 5].trim() ) ;
        float  low           = Float.parseFloat( row[ 6].trim() ) ;
        float  close         = Float.parseFloat( row[ 8].trim() ) ;
        long   totalTradeQty = Long.parseLong  ( row[10].trim() ) ;
        float  totalTradeVal = Float.parseFloat( row[11].trim() ) ;
        long   totalTrades   = Long.parseLong  ( row[12].trim() ) ;

        histRows = histRepo.findBySymbolAndDate( symbol, date ) ;
        if( histRows == null || histRows.isEmpty() ) {
            
            eodData = new HistoricEQData() ;
            
            eodData.setSymbol( symbol ) ;
            eodData.setDate( date ) ;
            eodData.setOpen( open ) ;
            eodData.setHigh( high ) ;
            eodData.setLow( low ) ;
            eodData.setClose( close ) ;
            eodData.setTotalTradeQty( totalTradeQty ) ;
            eodData.setTotalTradeVal( totalTradeVal ) ;
            eodData.setTotalTrades( totalTrades ) ;
            
            histRepo.saveAndFlush( eodData ) ;
        }
        else {
            deleteDuplicateHistoricData( histRows ) ;
        }
        
        return eodData ;
    }
    
    private void deleteDuplicateHistoricData( List<HistoricEQData> candles ) {
        
        if( candles != null && !candles.isEmpty() && candles.size()>1 ) {
            for( int i=1; i<candles.size()-1; i++ ) {
                HistoricEQData candle = candles.get( i ) ;
                log.debug( "Deleting duplicate candle for " + 
                           candle.getSymbol() + " @ " + 
                           SDF.format( candle.getDate()  ) ) ;
                
                histRepo.delete( candle ) ;
            }
        }
    }
}
