package com.sandy.capitalyst.server.job.equity.eodrefresh;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.io.StringReader ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.beanutils.BeanUtils ;
import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityTTMPerf ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTTMPerfRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo.ClosePrice ;
import com.univocity.parsers.csv.CsvParser ;
import com.univocity.parsers.csv.CsvParserSettings ;

public class EquityTTMPerfUpdater {
    
    private static final Logger log = Logger.getLogger( EquityTTMPerfUpdater.class ) ;
    
    private static final String LAST_1Y_EOD_URL = 
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
    
    private Map<String, HistoricEQData> todayEODCandles = new HashMap<>() ;
    private Map<String, EquityTTMPerf>  perfMap      = new HashMap<>() ;
    
    private HistoricEQDataRepo histRepo = null ;
    private EquityTTMPerfRepo  perfRepo = null ;
    
    private Object[][] ttmTimeMarkers = null ;

    public EquityTTMPerfUpdater() {
        histRepo = getBean( HistoricEQDataRepo.class ) ;
        perfRepo = getBean( EquityTTMPerfRepo.class ) ;
        
        generateTTMDateMarkers() ;
    }
    
    private void generateTTMDateMarkers() {
        
        Date today   = new Date() ;
        
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
        
        ttmTimeMarkers     = new Object[15][] ;
        ttmTimeMarkers[ 0] = new Object[]{ "perf1w" , back1W  } ;     
        ttmTimeMarkers[ 1] = new Object[]{ "perf2w" , back2W  } ;     
        ttmTimeMarkers[ 2] = new Object[]{ "perf1m" , back1M  } ;     
        ttmTimeMarkers[ 3] = new Object[]{ "perf2m" , back2M  } ;     
        ttmTimeMarkers[ 4] = new Object[]{ "perf3m" , back3M  } ;     
        ttmTimeMarkers[ 5] = new Object[]{ "perf4m" , back4M  } ;     
        ttmTimeMarkers[ 6] = new Object[]{ "perf5m" , back5M  } ;     
        ttmTimeMarkers[ 7] = new Object[]{ "perf6m" , back6M  } ;     
        ttmTimeMarkers[ 8] = new Object[]{ "perf7m" , back7M  } ;     
        ttmTimeMarkers[ 9] = new Object[]{ "perf8m" , back8M  } ;     
        ttmTimeMarkers[10] = new Object[]{ "perf9m" , back9M  } ;     
        ttmTimeMarkers[11] = new Object[]{ "perf10m", back10M } ;     
        ttmTimeMarkers[12] = new Object[]{ "perf11m", back11M } ;     
        ttmTimeMarkers[13] = new Object[]{ "perf12m", back12M } ;     
        ttmTimeMarkers[14] = new Object[]{ "perfFy" , fyStart } ;     
    }
    
    private Date getFYStartDate() {
        
        Calendar cal    = Calendar.getInstance() ;
        int      fyYear = cal.get( Calendar.YEAR ) ;
        int      month  = cal.get( Calendar.MONTH ) ;
        
        if( month >= Calendar.JANUARY && month <= Calendar.MARCH ) { 
            fyYear -= 1 ;
        }
        cal.set( fyYear, Calendar.APRIL, 1, 0, 0, 0 ) ;
        return cal.getTime() ;
    }
    
    public void addTodayEODCandle( HistoricEQData candle ) {
        todayEODCandles.put( candle.getSymbol(), candle ) ;
    }

    public void updateTTMPerfMeasures() throws Exception {
        
        preloadTTMPerfRecords() ;
        
        for( Object[] meta : ttmTimeMarkers ) {
            
            String milestoneName = ( String )meta[0] ;
            Date date    = ( Date )meta[1] ;
            
            log.debug( "Updating TTM " + milestoneName + " @" + SDF.format( date ) ) ;
            
            updateMilestonePerf( milestoneName, date ) ;
        }
        perfRepo.saveAll( perfMap.values() ) ;
    }
    
    private void preloadTTMPerfRecords() throws Exception {
        
        for( HistoricEQData todayCandle : todayEODCandles.values() ) {
            
            String symbol = todayCandle.getSymbol() ;
            
            EquityTTMPerf perf = perfRepo.findBySymbolNse( symbol ) ;
            if( perf == null ) {
                perf = new EquityTTMPerf() ;
                perf.setSymbolNse( todayCandle.getSymbol() ) ;
            }
            perf.setCurrentPrice( todayCandle.getClose() ) ;
            
            perfMap.put( todayCandle.getSymbol(), perf ) ;
        }
    }
    
    private void updateMilestonePerf( String perfField, Date date ) 
        throws Exception {
        
        List<ClosePrice>        histEODPriceList = null ;
        Map<String, ClosePrice> histEODPriceMap  = new HashMap<>() ;
        
        histEODPriceList = histRepo.getClosePriceNearestToDate( date ) ;
        for( ClosePrice histCP : histEODPriceList ) {
            histEODPriceMap.put( histCP.getSymbol(), histCP ) ;
        }
        
        for( HistoricEQData todayCandle : todayEODCandles.values() ) {
            
            String        symbol = todayCandle.getSymbol() ;
            EquityTTMPerf perf   = perfMap.get( symbol ) ;
            
            ClosePrice histCP = histEODPriceMap.get( symbol ) ;
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
        
        log.debug( "   Filling historic cap for " + symbol ) ;
        
        String DIV_START = "<div id='csvContentDiv' style='display:none;'>" ;
        String url = LAST_1Y_EOD_URL.replace( "{symbol}", symbol ) ;
        
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        String response = null ;
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
            List<String[]> records = null ;
            
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
            
            log.debug( "     " + numGapsFilled + " gaps filled." ) ;
        }
        
        perf.setGapsFilled( true ) ;
        perfRepo.saveAndFlush( perf ) ;
        
        return eodDataClosestToMilestone ;
    }
    
    private HistoricEQData addHistoricRecord( String[] row ) throws Exception {
        
        HistoricEQData eodData = null ;
        List<HistoricEQData> histRows = null ;
        
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
