package com.sandy.capitalyst.server.api.ledgermgmt.helpers.loadcalc;

import java.util.HashMap ;
import java.util.Map ;

public class CalendarUtil {

    public static final String[] MONTH_NAMES = {
            "Apr", "May", "Jun", "Jul", "Aug", "Sep", 
            "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
    } ;
    
    private static final Map<String, Integer> MONTH_IDX = new HashMap<>() ;
    
    static {
        for( int i=0; i<MONTH_NAMES.length; i++ ) {
            MONTH_IDX.put( MONTH_NAMES[i].toUpperCase(), i ) ;
        }
    }

    public static int getMonthIndex( String monthName ) {
        
        String key = monthName.trim().toUpperCase() ;
        if( MONTH_IDX.containsKey( key ) ) {
            return MONTH_IDX.get( key ) ;
        }
        throw new IllegalArgumentException( "Invalid month name " + monthName ) ;
    }
    
    public static String getMonthName( int index ) {
        return MONTH_NAMES[index] ;
    }
}
