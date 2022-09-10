package com.sandy.capitalyst.server.test.breeze;

import java.io.File ;

import org.apache.log4j.Logger ;

import com.fasterxml.jackson.core.JsonFactory ;
import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI ;
import com.sandy.capitalyst.server.breeze.api.BreezeGetPortfolioHoldingsAPI.PortfolioHolding ;
import com.sandy.capitalyst.server.breeze.internal.BreezeAPIResponse ;

public class BreezeResponseTester {

    private static final Logger log = Logger.getLogger( BreezeResponseTester.class ) ;
    
    public static void main( String[] args ) throws Exception {
        BreezeResponseTester tester = new BreezeResponseTester() ;
        tester.test() ;
    }
    
    public BreezeResponseTester() {}
    
    public void test() throws Exception {
        
        File jsonFile = new File( "/Users/sandeep/temp/portfolio-holding.json" ) ;
        ObjectMapper jsonParser = new ObjectMapper( new JsonFactory() ) ;
        JsonNode rootNode = jsonParser.readTree( jsonFile ) ;
        
        BreezeGetPortfolioHoldingsAPI api = new BreezeGetPortfolioHoldingsAPI() ;
        BreezeAPIResponse<PortfolioHolding> response = api.createResponse( rootNode ) ;
        
        for( PortfolioHolding h : response.getEntities() ) {
            log.debug( h.getSymbol() + " :: " + h.getQuantity() ) ;
        }
    }
}
