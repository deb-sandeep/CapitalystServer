package com.sandy.capitalyst.server.daemon.equity.portfolioupdate;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.Date ;
import java.util.List ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.BreezeException ;
import com.sandy.capitalyst.server.breeze.BreezeException.Type ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSessionManager ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPConfigGroup ;
import com.sandy.capitalyst.server.core.nvpconfig.NVPManager ;
import com.sandy.capitalyst.server.daemon.equity.intraday.EquityLTPRepository ;
import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal.TradingHolidayCalendar ;
import com.sandy.capitalyst.server.daemon.util.EventRateMonitor ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;

public class PortfolioMarketPriceUpdater extends Thread {

    private static final Logger log = Logger.getLogger( PortfolioMarketPriceUpdater.class ) ;

    private static PortfolioMarketPriceUpdater instance = null ;
    
    public static final String CFG_GRP_NAME = "PortfolioCMPUpdaterDaemon" ;
    
    public static final String CFG_PAUSE_REFRESH_FLAG = "pause_refresh" ;
    public static final String CFG_LIVE_REFRESH_DELAY = "refresh_delay_secs" ;
    public static final String CFG_PRINT_DEBUG_STMT   = "debug_enable" ;
    
    public static final int MIN_REFRESH_DELAY = 10 ;

    public static PortfolioMarketPriceUpdater instance() {
        if( instance == null ) {
            instance = new PortfolioMarketPriceUpdater() ;
        }
        return instance ;
    }
    
    private TradingHolidayCalendar holidayCalendar = null ;
    private BreezeGetPortfolioHoldingsAPI api = null ;
    
    // In the last 300 seconds, if we have 5 exceptions or more, the rate
    // monitor will keep the threshold breached flag till the time the 
    // count goes down below 5
    private EventRateMonitor genericERM   = new EventRateMonitor( 300, 5 ) ;
    private EventRateMonitor cmpUpdateERM = new EventRateMonitor( 300, 5 ) ;
    
    private EquityHoldingRepo ehRepo = null ;
    private EquityLTPRepository ltpRepository = null ;
    
    private boolean pauseRefresh = false ;
    private int     refreshDelay = 10 ;
    private boolean debugEnable  = false ;
    
    private PortfolioMarketPriceUpdater() {}

    public void initialize() throws Exception {
        
        this.holidayCalendar = new TradingHolidayCalendar() ;
        this.api             = new BreezeGetPortfolioHoldingsAPI() ;
        this.ehRepo          = getBean( EquityHoldingRepo.class ) ;
        this.ltpRepository   = getBean( EquityLTPRepository.class ) ;
    }
    
    public void run() {
        
        while( true ) {
            try {
                if( holidayCalendar.isMarketOpenNow() && 
                    !genericERM.hasThresholdBreached() ) {
                    
                    refreshConfiguration() ;
                    
                    if( pauseRefresh ) {
                        if( debugEnable ) {
                            log.debug( "Portfolio CMP refresh paused." ) ;
                        }
                    }
                    else {
                        updateCurrentMktPriceInPortfolio() ;
                    }
                    
                    // 15 seconds gap -> 1680 calls per trading day
                    // 10 seconds gap -> 2500 calls
                    //  5 seconds gap -> 5040 calls
                    TimeUnit.SECONDS.sleep( refreshDelay ) ;
                }
                else {
                    TimeUnit.MINUTES.sleep( 2 ) ;
                }
            }
            catch( InterruptedException e ) {
                // Don't worry about it.
            }
            catch( Exception e ) {
                
                log.error( "Unanticipated error.", e ) ;
                genericERM.registerEvent() ;
                
                try {
                    TimeUnit.SECONDS.sleep( 10 ) ;
                }
                catch( InterruptedException ie ) {
                    log.error( "CMP daemon exception stall interrrupted.", ie ) ;
                    break ;
                }
            }
        }
    }
    
    private void refreshConfiguration() {
        
        NVPManager nvpMgr = NVPManager.instance() ;
        NVPConfigGroup cfg = nvpMgr.getConfigGroup( CFG_GRP_NAME ) ; ;

        pauseRefresh = cfg.getBoolValue( CFG_PAUSE_REFRESH_FLAG, pauseRefresh ) ;
        refreshDelay = cfg.getIntValue ( CFG_LIVE_REFRESH_DELAY, refreshDelay ) ;
        debugEnable  = cfg.getBoolValue( CFG_PRINT_DEBUG_STMT,   debugEnable  ) ;
        
        // Hardening - Prevents accidental setting of refresh delay to a lower
        // value which will result in a tight loop and API rate threshold
        // breach.
        if( refreshDelay < MIN_REFRESH_DELAY ) {
            refreshDelay = MIN_REFRESH_DELAY ;
            cfg.setValue( CFG_LIVE_REFRESH_DELAY, MIN_REFRESH_DELAY ) ;
        }
    }
    
    private void updateCurrentMktPriceInPortfolio() {
        
        
        List<BreezeCred> credentials = Breeze.instance().getAllCreds() ;
        
        for( BreezeCred cred : credentials ) {
            
            if( debugEnable ) {
                log.debug( "" ) ;
                log.debug( "Updating Portfolio CMP for " + cred.getUserName() ) ;
            }
            
            if( !cred.isHasPortfolio() ) {
                if( debugEnable ) {
                    log.debug( "  No portfolio exists for " + cred.getUserName() ) ;
                }
                continue ;
            }
            
            if( !Breeze.instance().hasActiveSession( cred ) ) {
                if( debugEnable ) {
                    log.debug( "  Skipping CMP update. No active session." ) ;
                }
                continue ;
            }
            
            if( BreezeSessionManager.instance().isWithinDayRateLimit( cred ) ) {
                
                if( !cmpUpdateERM.hasThresholdBreached( cred.getUserId() ) ) {
                    
                    try {
                        updateCurrentMktPriceInPortfolio( cred ) ;
                    }
                    catch( BreezeException e ) {
                        if( e.getType() != Type.API_DAY_LIMIT_EXCEED ) {
                            log.error( "Exception while updating CMP." ) ;
                            log.error( e ) ;
                        }
                        
                        cmpUpdateERM.registerEvent( cred.getUserId() ) ;
                    }
                }
                else {
                    log.info( "  " + cred.getUserId() + 
                              " has breached exception limit. " + 
                              "Update CMP stalled for some time." ) ;
                }
            }
            else {
                if( debugEnable ) {
                    log.debug( "  Breeze daily rate limit reached." ) ;
                }
            }
        }
    }
    
    private void updateCurrentMktPriceInPortfolio( BreezeCred cred ) 
        throws BreezeException {
        
        BreezeAPIResponse<PortfolioHolding> response = null ;
        
        response = api.execute( cred ) ;
        
        if( response == null ) {
            log.info( "  Get portfolio CMP failed. " + cred.getUserName() ) ;
            log.info( "    Reason : Server response is null." ) ;
        }
        else if( !response.isError() ) {
            Date curTime = new Date() ;
            updateCurrentMktPrice( response, curTime ) ;
        }
        else {
            log.info( "  Get portfolio CMP failed. " + cred.getUserName() ) ;
            log.info( "    Reason : " + response.getError() ) ;
        }
    }
    
    private void updateCurrentMktPrice( 
                  BreezeAPIResponse<PortfolioHolding> response, Date curTime ) {
        
        List<PortfolioHolding> holdings = null ;
        
        holdings = response.getEntities() ;

        for( PortfolioHolding holding : holdings ) {
            
            String symbolIcici = holding.getSymbol() ;
            float  curMktPrice = holding.getCurrentMktPrice() ;
            float  change      = holding.getChange() ;
            
            updateCMP( symbolIcici, curMktPrice, change, curTime ) ;
        }
    }
    
    private void updateCMP( String symbolIcici, float currPrice, 
                            float change, Date curTime ) {
        
        List<EquityHolding> ehList = ehRepo.findBySymbolIcici( symbolIcici ) ;
        for( EquityHolding eh : ehList ) {
            
            if( debugEnable ) {
                log.debug( "  Updating CMP for " + symbolIcici ) ;
            }
            
            eh.setCurrentMktPrice( currPrice ) ;
            eh.setDayGain( change * eh.getQuantity() ) ;
            eh.setLastUpdate( curTime ) ;
            
            ehRepo.save( eh ) ;
            ltpRepository.addSnapshot( eh ) ;
        }
    }
}
