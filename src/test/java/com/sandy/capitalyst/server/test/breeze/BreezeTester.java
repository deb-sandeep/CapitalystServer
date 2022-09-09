package com.sandy.capitalyst.server.test.breeze;

import java.io.File ;
import java.lang.reflect.Method ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetDmatHoldingsAPI ;
import com.sandy.common.util.ReflectionUtil ;

public class BreezeTester {

    private static final Logger log = Logger.getLogger( BreezeTester.class ) ;
    
    public static void main( String[] args ) throws Exception {
        BreezeTester tester = new BreezeTester( args ) ;
        tester.test() ;
    }
    
    private String ucId   = "getDmatHoldings" ;
    
    public BreezeTester( String[] args ) {
        
        log.debug( "----------------= BreezeSessionManager Test =--------------------" ) ;
        log.debug( "   ucId   = " + ucId ) ;
    }
    
    public void test() throws Exception {
        
        File configFile = new File( "/Users/sandeep/projects/workspace/capitalyst/breeze/config/breeze-config.yaml" ) ;
        Breeze breeze = Breeze.instance() ;
        breeze.initialize( configFile ) ;
        
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
}
