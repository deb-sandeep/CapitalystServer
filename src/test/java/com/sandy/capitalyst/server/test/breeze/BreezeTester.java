package com.sandy.capitalyst.server.test.breeze;

import static org.apache.commons.lang.StringUtils.leftPad;
import static org.apache.commons.lang.StringUtils.rightPad;

import java.io.File ;
import java.lang.reflect.Method ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI.TradeDetail ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI.Trade ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;
import com.sandy.common.util.ReflectionUtil ;

public class BreezeTester {

    private static final Logger log = Logger.getLogger( BreezeTester.class ) ;
    
    public static void main( String[] args ) throws Exception {
        BreezeTester tester = new BreezeTester( args ) ;
        tester.test() ;
    }
    
    public class BreezeListener implements BreezeAPIInvocationListener {

        @Override
        public void preBreezeCall( APIInvocationInfo info ) {
            log.debug( info ) ;
        }

        @Override
        public void postBreezeCall( APIInvocationInfo info ) {
            log.debug( info ) ;
        }
    }
    
    private String ucId = "getTrades" ;
    
    public BreezeTester( String[] args ) {
        
        log.debug( "----------------= BreezeSessionManager Test =--------------------" ) ;
        log.debug( "   ucId   = " + ucId ) ;
    }
    
    public void test() throws Exception {
        
        File configFile = new File( "/Users/sandeep/projects/workspace/capitalyst/breeze/config/breeze-config.yaml" ) ;
        Breeze breeze = Breeze.instance() ;
        breeze.initialize( configFile ) ;
        breeze.addInvocationListener( new BreezeListener() ) ;
        
        if( ucId != null ) {
            log.debug( "\nInvoking use case - " + ucId ) ;
            log.debug( "-------------------------------------------" ) ;
            
            Method method =  ReflectionUtil.findMethod( BreezeTester.class, ucId, null ) ;
            method.invoke( this ) ;
        }
    }
    
    @SuppressWarnings( "unused" )
    private void getDmatHoldings() throws Exception {
        
        BreezeGetDmatHoldingsAPI api = new BreezeGetDmatHoldingsAPI() ;
        List<BreezeCred> creds = Breeze.instance().getAllCreds() ;
        for( BreezeCred cred : creds ) {
            api.execute( cred ) ;
        }
        /*
        BreezeCred cred = Breeze.instance().getCred( "sovadeb" ) ;
        api.execute( cred ) ;
        */
    }

    @SuppressWarnings( "unused" )
    private void getPortfolioHoldings() throws Exception {
        
        BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
        api.setStockCode( "ADAPOR" ) ;
        /*
        List<BreezeCred> creds = Breeze.instance().getAllCreds() ;
        for( BreezeCred cred : creds ) {
            api.execute( cred ) ;
        }
         */
        BreezeCred cred = Breeze.instance().getCred( "sandkumb23" ) ;
        BreezeAPIResponse<PortfolioHolding> response = api.execute( cred ) ;
        
        for( PortfolioHolding h : response.getEntities() ) {
            log.debug( h.getSymbol() + " :: " + h.getQuantity() + " :: " + h.getAveragePrice() ) ;
        }
    }

    @SuppressWarnings( "unused" )
    private void getTrades() throws Exception {
        
        BreezeGetTradeListAPI api = new BreezeGetTradeListAPI() ;
        api.setFromDate( DateUtils.addYears( new Date(), -20 ) ) ;
        //api.setFromDate( DateUtils.addDays( new Date(), -5 ) ) ;
        
        BreezeCred cred = Breeze.instance().getCred( "sovadeb" ) ;
        BreezeAPIResponse<Trade> response = api.execute( cred ) ;
        
        SimpleDateFormat sdf = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
        
        for( Trade trade : response.getEntities() ) {
            log.debug( sdf.format( trade.getTradeDate() ) + " | " + 
                       rightPad( trade.getSymbolIcici(), 7) + " | " +
                       rightPad( trade.getAction(), 5) + " | " + 
                       leftPad( "" + trade.getQuantity(), 4) + " | " + 
                       trade.getOrderId() ) ;
        }
    }

    @SuppressWarnings( "unused" )
    private void getTradeDetail() throws Exception {
        
        BreezeGetTradeDetailAPI api = new BreezeGetTradeDetailAPI() ;
        api.setOrderId( "20220919N800053166" ) ;
        
        BreezeCred cred = Breeze.instance().getCred( "sandkumb23" ) ;
        BreezeAPIResponse<TradeDetail> response = api.execute( cred ) ;
        
        SimpleDateFormat sdf = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;
        
        for( TradeDetail detail : response.getEntities() ) {
            log.debug( sdf.format( detail.getTxnDate() ) + " | " + 
                       leftPad( "" + detail.getQuantity(), 5) + " | " +
                       leftPad( "" + detail.getTxnPrice(), 6) + " | " + 
                       leftPad( "" + detail.getBrokerage(), 4) + " | " ) ;
        }
    }
}
