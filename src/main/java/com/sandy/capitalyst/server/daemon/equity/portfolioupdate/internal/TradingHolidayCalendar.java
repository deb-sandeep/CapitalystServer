package com.sandy.capitalyst.server.daemon.equity.portfolioupdate.internal;

import java.io.InputStream ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Collection ;
import java.util.Date ;
import java.util.GregorianCalendar ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;
import org.springframework.stereotype.Component ;

import com.fasterxml.jackson.annotation.JsonFormat ;
import com.fasterxml.jackson.databind.JsonNode ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory ;

import lombok.Data ;

@Component
public class TradingHolidayCalendar {

    static final Logger log = Logger.getLogger( TradingHolidayCalendar.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private ObjectMapper yamlParser = new ObjectMapper( new YAMLFactory() ) ;
    
    @Data
    public static class Holiday {
        
        @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy" )
        private Date date ;
        private String day ;
        private String name ;
    }
    
    private Map<String, Holiday> holidayMap = new HashMap<>() ;
    
    public TradingHolidayCalendar() throws Exception {

        InputStream  holidayCfg = null ;
        
        holidayCfg = this.getClass().getResourceAsStream( "/config/nse-holidays.yaml" ) ;
        
        yamlParser.findAndRegisterModules() ;
        JsonNode rootNode = yamlParser.readTree( holidayCfg ) ;
        extractHolidays( rootNode ) ;
    }
    
    private void extractHolidays( JsonNode rootNode ) throws Exception {
        
        for( int i=0; i<rootNode.size(); i++ ) {
            JsonNode childNode = rootNode.get( i ) ;
            Holiday h = yamlParser.treeToValue( childNode, Holiday.class ) ;
            
            holidayMap.put( SDF.format( h.getDate() ), h ) ;
            
            //log.debug( "Holiday " + SDF.format( h.getDate() ) + " :: " +
            //           h.getName() ) ;
        }
    }
    
    public boolean isHolidayToday() {
        return isHoliday( new Date() ) ;
    }
    
    public boolean isHoliday( Date date ) {
        
        boolean isHoliday = false ;
        
        if( holidayMap.containsKey( SDF.format( date ) ) ) {
            isHoliday = true ;
        }
        else {
            Calendar cal = new GregorianCalendar() ;
            cal.setTime( date ) ;

            int day = cal.get( Calendar.DAY_OF_WEEK ) ;
            if( day == Calendar.SATURDAY || day == Calendar.SUNDAY ) {
                isHoliday = true ;
            }
        }
        return isHoliday ;
    }
    
    public Collection<Holiday> getHolidays() {
        return holidayMap.values() ;
    }
    
    public boolean isMarketOpenNow() {
        return isMarketOpenOnDate( new Date() ) ;
    }
    
    public boolean isMarketOpenOnDate( Date date ) {
        
        if( !isHoliday( date ) ) {
            Calendar cal = Calendar.getInstance() ;
            cal.setTime( date ) ;
            
            int hour = cal.get( Calendar.HOUR_OF_DAY ) ;
            int min  = cal.get( Calendar.MINUTE ) ;
            
            if( hour >= 10 && hour < 15 ) {
                return true ;
            }
            else if( hour == 15 && min <= 35 ) {
                return true ;
            }
            else if( hour ==9 && min >= 15 ) {
                return true ;
            }
        }
        return false ;
    }
}
