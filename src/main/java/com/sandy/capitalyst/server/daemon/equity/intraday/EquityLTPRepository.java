package com.sandy.capitalyst.server.daemon.equity.intraday;

import static com.sandy.capitalyst.server.core.util.StringUtil.DD_MMM_YYYY ;

import java.util.Date ;
import java.util.HashMap ;
import java.util.Map ;

import javax.annotation.PostConstruct ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.sandy.capitalyst.server.daemon.equity.intraday.EquityITDSnapshotService.ITDSnapshot ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQITDData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQITDDataRepo ;

import lombok.Data ;

/**
 * This singleton component maintains the latest price and percentage change
 * for the equity stocks being tracked.
 * 
 * This component boots during startup and initializes it with the latest
 * EOD price and perf1D. Post that any recent intraday data, either through
 * the Breeze portfolio updates (takes preference) or NSE intraday data.
 * 
 * Post the initialization this class relies on daemons to keep it updated. If
 * a LTP for a stock which is not being tracked is queried, a null value is
 * returned.
 */
@Component( "eqLTPRepository" )
public class EquityLTPRepository {

    private static final Logger log = Logger.getLogger( EquityLTPRepository.class ) ;
    
    @Data
    public static class LTP {
        private String symbolNse = null ;
        private float  price     = 0F ;
        private float  pchange   = 0F ;
        private Date   time      = null ;
        
        private LTP( ITDSnapshot snapshot ) {
            
            this.symbolNse = snapshot.getSymbol() ;
            this.price     = snapshot.getPrice() ;
            this.pchange   = snapshot.getPChange() ;
            this.time      = snapshot.getTime() ;
        }
        
        private LTP( HistoricEQData histEod ) {
            
            this.symbolNse = histEod.getSymbol() ;
            this.price     = histEod.getClose() ;
            this.pchange   = histEod.getPctChange() ;
            this.time      = histEod.getDate() ;
            
            this.time = DateUtils.addHours( this.time, 16 ) ;
        }
        
        public LTP( EquityHolding h ) {
            
            float dayGainPerShare = ( h.getDayGain() / h.getQuantity() ) ;
            float lastClose = h.getCurrentMktPrice() - dayGainPerShare ;
            
            this.symbolNse = h.getSymbolNse() ;
            this.price     = h.getCurrentMktPrice() ;
            this.pchange   = (dayGainPerShare / lastClose)*100 ;
            this.time      = h.getLastUpdate() ;
        }
    }
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private HistoricEQDataRepo eodRepo = null ;
    
    @Autowired
    private HistoricEQITDDataRepo itdRepo = null ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    private Map<String, LTP> itdSnapshots = new HashMap<>() ;
    
    public void addSnapshot( ITDSnapshot snapshot ) {
        addLTP( new LTP( snapshot ) ) ;
    }
    
    public void addSnapshot( HistoricEQData histEod ) {
        addLTP( new LTP( histEod ) ) ;
    }
    
    public void addSnapshot( EquityHolding h ) {
        addLTP( new LTP( h ) ) ;
    }
    
    public void addLTP( LTP ltp ) {
        
        String symbolNse = ltp.getSymbolNse() ;
        LTP existingLTP = itdSnapshots.get( symbolNse ) ;
        
        if( existingLTP == null ) {
            itdSnapshots.put( symbolNse, ltp ) ;
        }
        else {
            if( ltp.getTime().after( existingLTP.getTime() ) ) {
                itdSnapshots.put( symbolNse, ltp ) ;
                log.debug( "    > " + symbolNse ) ;
            }
        }
    }
    
    public LTP getLTP( String symbol ) {
        return itdSnapshots.get( symbol ) ;
    }

    @PostConstruct
    public void init() {
        
        log.debug( "Initializing equity LTP repository" ) ;
        loadLatestEndOfDayPrice() ;
        loadHoldingCMP() ;
        loadITDSnapshots() ;
        log.debug( "quity LTP repository initialized" ) ;
    }
    
    private void loadLatestEndOfDayPrice() {
        
        log.debug( "  Loading latest end of day price." ) ;
        Date latestEodDate = eodRepo.findLatestRecordDate() ;
        
        log.debug( "    latest eod date = " + DD_MMM_YYYY.format( latestEodDate ) ) ;
        eodRepo.findByDate( latestEodDate )
               .forEach( this::addSnapshot ) ;
        
        log.debug( "    # EOD records = " + itdSnapshots.size() ) ;
    }
    
    private void loadHoldingCMP() {
        
        log.debug( "  Loading CMP of holdings." ) ;
        ehRepo.findNonZeroHoldings()
              .forEach( this::addSnapshot ) ;
    }
    
    private void loadITDSnapshots() {
        
        log.debug( "  Loading latest ITD snapshots." ) ;
        final int[] counter = new int[1] ;
        itdSnapshots.forEach( (symbol,ltp) -> {
            
            EquityMaster em = emRepo.findBySymbol( symbol ) ;
            HistoricEQITDData itd = itdRepo.getLatestITDData( em.getId() ) ;
            
            if( itd != null && itd.getTime().after( ltp.getTime() )) {
                ltp.time    = itd.getTime() ;
                ltp.price   = itd.getPrice() ;
                ltp.pchange = itd.getPChange() ;
                log.debug( "    > " + symbol ) ;
                counter[0]++ ;
            }
        } ) ;
        
        log.debug( "    # ITD records = " + counter[0] ) ;
    }
}
