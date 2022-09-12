package com.sandy.capitalyst.server.daemon.equity.portfolioupdate;

import org.apache.log4j.Logger ;

public class PortfolioMarketPriceUpdater extends Thread {

    private static final Logger log = Logger.getLogger( PortfolioMarketPriceUpdater.class ) ;

    private static PortfolioMarketPriceUpdater instance = null ;
    
    public static PortfolioMarketPriceUpdater instance() {
        if( instance == null ) {
            instance = new PortfolioMarketPriceUpdater() ;
        }
        return instance ;
    }

    public void initialize() throws Exception {
    }
    
    public void run() {
        while( true ) {
            try {
                if( okForNextRun() ) {
                    
                }
                else {
                    Thread.sleep( 60*1000 ) ;
                }
            }
            catch( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean okForNextRun() {
        return true ;
    }
}
