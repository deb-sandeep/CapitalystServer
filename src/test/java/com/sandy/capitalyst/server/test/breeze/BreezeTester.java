package com.sandy.capitalyst.server.test.breeze;

import static org.apache.commons.lang.StringUtils.leftPad;
import static org.apache.commons.lang.StringUtils.rightPad;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeAPIInvocationListener ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI.DmatHolding ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeDetailAPI.TradeDetail ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetTradeListAPI.Trade ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;
import com.sandy.common.util.StringUtil ;

public class BreezeTester {

    private static final Logger log = Logger.getLogger( BreezeTester.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    public static void main( String[] args ) throws Exception {
        BreezeTester tester = new BreezeTester() ;
        tester.test() ;
    }
    
    public class BreezeListener implements BreezeAPIInvocationListener {

        public void preBreezeCall( APIInvocationInfo info ) {
            log.debug( info ) ;
        }

        public void postBreezeCall( APIInvocationInfo info ) {
            log.debug( info ) ;
        }
    }
    
    private BreezeCred cred = null ;
    
    public void test() throws Exception {
        
        File configFile = new File( "/Users/sandeep/projects/workspace/capitalyst/breeze/config/breeze-config.yaml" ) ;
        Breeze breeze = Breeze.instance() ;
        breeze.initialize( configFile ) ;
        breeze.addInvocationListener( new BreezeListener() ) ;

        cred = Breeze.instance().getCred( "sandkumb23" ) ;
        
        //getPortfolioHoldings() ;
        //getTrades( "25-Sep-2022", "26-Sep-2022" ) ;
        //getTradeDetail( null ) ;
        getDmatHoldings() ;
    }
    
    public void getDmatHoldings() throws Exception {
        
        BreezeGetDmatHoldingsAPI api = new BreezeGetDmatHoldingsAPI() ;
        BreezeAPIResponse<DmatHolding> response = api.execute( cred ) ;
        
        response.getEntities().forEach( h -> {
            
            log.debug( h.getSymbol() + " :: " + 
                       leftPad( ""+h.getQuantity(), 5 ) ) ;
        }) ;
    }

    public void getPortfolioHoldings() throws Exception {
        
        BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
        BreezeAPIResponse<PortfolioHolding> response = api.execute( cred ) ;
        
        for( PortfolioHolding h : response.getEntities() ) {
            log.debug( h.getSymbol() + " :: " + 
                       leftPad( ""+h.getQuantity(), 5 ) + " :: " + 
                       leftPad( ""+h.getAveragePrice(), 10 ) ) ;
        }
    }

    public void getTrades( String fromDate, String toDate ) throws Exception {
        
        BreezeGetTradeListAPI api = new BreezeGetTradeListAPI() ;
        api.setFromDate( SDF.parse( fromDate ) ) ;
        if( StringUtil.isEmptyOrNull( toDate ) ) {
            api.setToDate( new Date() ) ;
        }
        else {
            api.setToDate( SDF.parse( toDate ) ) ;
        }
        
        BreezeAPIResponse<Trade> response = api.execute( cred ) ;
        
        for( Trade trade : response.getEntities() ) {
            log.debug( SDF.format( trade.getTradeDate() ) + " | " + 
                       rightPad( trade.getSymbolIcici(), 7) + " | " +
                       rightPad( trade.getAction(), 5) + " | " + 
                       leftPad( "" + trade.getQuantity(), 4) + " | " + 
                       trade.getOrderId() ) ;
        }
    }

    public void getTradeDetail( String orderId ) throws Exception {
        
        BreezeGetTradeDetailAPI api = new BreezeGetTradeDetailAPI() ;
        if( StringUtil.isEmptyOrNull( orderId ) ) {
            api.setOrderId( "20220919N800053166" ) ;
        }
        else {
            api.setOrderId( orderId ) ;
        }
        
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
