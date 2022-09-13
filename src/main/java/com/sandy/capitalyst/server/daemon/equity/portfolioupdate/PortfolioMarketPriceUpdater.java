package com.sandy.capitalyst.server.daemon.equity.portfolioupdate;

import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;
import com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal.TradingHolidayCalendar ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

public class PortfolioMarketPriceUpdater extends Thread {

    private static final Logger log = Logger.getLogger( PortfolioMarketPriceUpdater.class ) ;

    private static PortfolioMarketPriceUpdater instance = null ;
    private static boolean DEBUG_ENABLE = false ;
    
    public static PortfolioMarketPriceUpdater instance() {
        if( instance == null ) {
            instance = new PortfolioMarketPriceUpdater() ;
        }
        return instance ;
    }
    
    private TradingHolidayCalendar holidayCalendar = null ;
    private BreezeGetPortfolioHoldingsAPI api = null ;
    private EquityHoldingRepo ehRepo = null ;
    
    private PortfolioMarketPriceUpdater() {}

    public void initialize() throws Exception {
        this.holidayCalendar = new TradingHolidayCalendar() ;
        this.api = new BreezeGetPortfolioHoldingsAPI() ;
        this.ehRepo = getBean( EquityHoldingRepo.class ) ;
    }
    
    public void run() {
        while( true ) {
            try {
                if( holidayCalendar.isMarketOpenNow() ) {
                    updateCurrentMktPriceInPortfolio() ;
                    // 10 seconds gap implies ~2500 calls in a full trading day
                    //  5 seconds gap, implies 5040.
                    Thread.sleep( 15*1000 ) ;
                }
                else {
                    Thread.sleep( 2*60*1000 ) ;
                }
            }
            catch( Exception e ) {
                log.error( "Error updating portfolio current mkt price.", e ) ;
            }
        }
    }
    
    private void updateCurrentMktPriceInPortfolio() 
        throws Exception {
        
        BreezeAPIResponse<PortfolioHolding> response = null ;
        
        List<BreezeCred> credentials = Breeze.instance().getAllCreds() ;
        
        for( BreezeCred cred : credentials ) {
            if( DEBUG_ENABLE ) {
                log.debug( "Updating Portfolio CMP for " + cred.getUserName() ) ;
            }
            response = api.execute( cred ) ;
            if( response.getStatus() == 200 ) {
                Date curTime = new Date() ;
                updateCurrentMktPrice( response, curTime ) ;
            }
            else {
                log.info( "Get portfolio CMP failed. " + cred.getUserName() ) ;
                log.info( "  Reason : " + response.getError() ) ;
            }
        }
    }
    
    private void updateCurrentMktPrice( BreezeAPIResponse<PortfolioHolding> response,
                                        Date curTime ) {
        
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
            
            if( DEBUG_ENABLE ) {
                log.debug( "Updating intra-day CMP for " + symbolIcici ) ;
            }
            
            eh.setCurrentMktPrice( currPrice ) ;
            eh.setDayGain( change * eh.getQuantity() ) ;
            eh.setLastUpdate( curTime ) ;
            
            ehRepo.save( eh ) ;
        }
    }
}
