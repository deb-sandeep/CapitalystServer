package com.sandy.capitalyst.server.test.breeze;

import java.io.File ;
import java.lang.reflect.Method ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.BreezeDmatHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.BreezeGetQuotesAPI ;
import com.sandy.capitalyst.server.breeze.BreezeGetTradeDetailAPI ;
import com.sandy.capitalyst.server.breeze.BreezeGetTradeListAPI ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession ;
import com.sandy.capitalyst.server.breeze.internal.BreezeSession.Session ;
import com.sandy.common.util.ReflectionUtil ;

public class BreezeTester {

    private static final Logger log = Logger.getLogger( BreezeTester.class ) ;
    
    public static void main( String[] args ) throws Exception {
        BreezeTester tester = new BreezeTester( args ) ;
        tester.test() ;
    }
    
    private String appKey = null ;
    private String userId = null ;
    private String passwd = null ;
    private String dob    = null ;
    private String secret = null ;
    private String ucId   = "getTradeDetail" ;
    
    public BreezeTester( String[] args ) {
        
        this.appKey = args[0] ;
        this.userId = args[1] ;
        this.passwd = args[2] ;
        this.dob    = args[3] ;
        this.secret = args[4] ;
        
        log.debug( "----------------= BreezeSession Test =--------------------" ) ;
        log.debug( "Command line parameters:" ) ;
        log.debug( "   AppKey = " + appKey ) ;
        log.debug( "   userId = " + userId ) ;
        log.debug( "   passwd = " + passwd ) ;
        log.debug( "   dob    = " + dob ) ;
        log.debug( "   secret = " + secret ) ;
        log.debug( "   ucId   = " + ucId ) ;
    }
    
    public void test() throws Exception {
        
        File persistDir = new File( System.getProperty( "user.home" ), "temp" ) ;
        BreezeSession breeze = BreezeSession.instance() ;
        breeze.initialize( appKey, userId, passwd, dob, secret, persistDir ) ;
        
        if( ucId != null ) {
            log.debug( "\nInvoking use case - " + ucId ) ;
            Method method =  ReflectionUtil.findMethod( BreezeTester.class, ucId, null ) ;
            method.invoke( this ) ;
        }
    }
    
    @SuppressWarnings( "unused" )
    private void generateSessionId() throws Exception {
        
        Session session = BreezeSession.instance().getSession() ;
        log.debug( "Session id    = " + session.getSessionId() ) ;
        log.debug( "Session token = " + session.getSessionToken() ) ;
    }
    
    @SuppressWarnings( "unused" )
    private void getDmatHoldings() throws Exception {
        
        BreezeDmatHoldingsAPI api = new BreezeDmatHoldingsAPI() ;
        api.getDmatHoldings() ;
    }
    
    @SuppressWarnings( "unused" )
    private void getPortfolioHoldings() throws Exception {
        
        BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
        api.getPortfolioHoldings() ;
    }

    @SuppressWarnings( "unused" )
    private void getQuotes() throws Exception {
        
        BreezeGetQuotesAPI api = new BreezeGetQuotesAPI() ;
        api.getQuotes() ;
    }

    @SuppressWarnings( "unused" )
    private void getTradeList() throws Exception {
        
        BreezeGetTradeListAPI api = new BreezeGetTradeListAPI() ;
        api.getTradeList() ;
    }

    @SuppressWarnings( "unused" )
    private void getTradeDetail() throws Exception {
        
        BreezeGetTradeDetailAPI api = new BreezeGetTradeDetailAPI() ;
        api.getTradeDetail() ;
    }
}
