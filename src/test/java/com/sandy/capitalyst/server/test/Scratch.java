package com.sandy.capitalyst.server.test;

import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

public class Scratch {

    private static final Logger log = Logger.getLogger( Scratch.class ) ;
    
    private static final String I = "        " ;
    
    public static void main( String[] args ) throws Exception {
        
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd" ) ;
        Date start = sdf.parse( "2021/06/01" ) ;
        Date end   = sdf.parse( "2022/09/30" ) ;
        Date date  = start ;
        
        List<Long>    labels = new ArrayList<>() ;
        List<Integer> eod    = new ArrayList<>() ;
        
        Map<Long, Integer[]> buyData  = new TreeMap<>() ;
        Map<Long, Integer[]> sellData = new TreeMap<>() ;
        
        int totalQuantity  = 0 ;
        
        while( date.before( end ) ) {
            
            long time = date.getTime() ;
            int trend = Math.random() > 0.4 ? 1 : -1 ;
            int price = 100 ;
            
            if( !eod.isEmpty() ) {
                price = (int)( eod.get( eod.size()-1 ) + trend*Math.random()*5 ) ;
                if( price <= 0 ) {
                    price = 25 ;
                }
            }

            labels.add( time ) ;
            eod.add( price ) ;
            
            double random = Math.random() ;
            if( random > 0.9 ) {
                int quantity = (int)(1 + 10*Math.random()) ;
                
                if( random > 0.95 ) {
                    buyData.put( time, new Integer[]{ price, quantity } ) ;
                    totalQuantity  += quantity ;
                }
                else {
                    quantity = quantity > totalQuantity ? totalQuantity : quantity ;
                    sellData.put( time, new Integer[]{ price, quantity } ) ;
                    totalQuantity -= quantity ;
                }
            }
            
            date = DateUtils.addDays( date, 1 ) ;
        }
        
        printLabels( labels ) ;
        printEoDData( eod ) ;
        printBuySellData( buyData, true ) ;
        printBuySellData( sellData, false ) ;
        printAvgPriceData( labels ) ;
    }

    private static void printAvgPriceData( List<Long> labels ) {
        
        log.debug( I + "var avgData = [" ) ;
        log.debug( I + "  {x:" + labels.get( 0 ) +", y:105}," ) ;
        log.debug( I + "  {x:" + labels.get( labels.size()-1 ) +", y:105}," ) ;
        log.debug( I + "] ;\n" ) ; 
    }

    private static void printLabels( List<Long> labels ) {
        
        log.debug( I + "var labels = [" ) ;
        StringBuilder sb = new StringBuilder() ;
        for( int i=0; i<labels.size(); i++ ) {
            
            Long l = labels.get( i ) ;
            
            if( i%6 == 0 ) {
                sb.append( I + "  " ) ;
            }
            sb.append( l.toString() + ", " ) ;
            if( i%6 == 5 ) {
                sb.append( "\n" ) ;
            }
        }
        log.debug( sb ) ;
        log.debug( I + "] ;\n" ) ; 
    }
    
    private static void printEoDData( List<Integer> eod ) {
        
        log.debug( I + "var eodData = [" ) ;
        StringBuilder sb = new StringBuilder() ;
        for( int i=0; i<eod.size(); i++ ) {
            
            Integer l = eod.get( i ) ;
            
            if( i%20 == 0 ) {
                sb.append( I + "  " ) ;
            }
            sb.append( l.toString() + ", " ) ;
            if( i%20 == 19 ) {
                sb.append( "\n" ) ;
            }
        }
        log.debug( sb ) ;
        log.debug( I + "] ;\n" ) ; 
    }

    private static void printBuySellData( Map<Long, Integer[]> datePriceMap,
                                          boolean buy ) {
        
        if( buy ) {
            log.debug( I + "var buyData = [" ) ;
        }
        else {
            log.debug( I + "var sellData = [" ) ;
        }
        
        datePriceMap.forEach( (key,value) -> {
            log.debug( I + "  {x:" + key +", y:" + value[0] + ", q:" + value[1] + "}," ) ;
        });
        log.debug( I + "] ;\n" ) ;
    }
    
    
}
