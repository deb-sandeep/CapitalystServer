package com.sandy.capitalyst.server.api.index.helper;

import java.io.InputStream ;
import java.util.HashMap ;
import java.util.List ;

import org.apache.commons.io.IOUtils ;
import org.apache.log4j.Logger ;

public class IndexHistKeyMap {

    private static final Logger log = Logger.getLogger( IndexHistKeyMap.class ) ;
    
    private static final String CFG_FILE_PATH = "/config/index-histname-map.properties" ;
    
    private static IndexHistKeyMap instance = null ;
    
    private static IndexHistKeyMap instance() {
        if( instance == null ) {
            try {
                instance = new IndexHistKeyMap() ;
            }
            catch( Exception e ) {
                log.error( "Error loading index translation properties.", e ) ;
            }
        }
        return instance ;
    }
    
    public static String translate( String indexName ) {
        IndexHistKeyMap map = instance() ;
        return map.getKey( indexName ) ;
    }
    
    private HashMap<String, String> translationMap = new HashMap<>() ;
    
    private IndexHistKeyMap() throws Exception {
        
        InputStream is = IndexHistKeyMap.class.getResourceAsStream( CFG_FILE_PATH ) ;
        if( is != null ) {
            List<String> lines = IOUtils.readLines( is ) ;
            lines.forEach( str -> {
                String[] parts = str.split( "=" ) ;
                translationMap.put( parts[0].trim().toUpperCase(), 
                                    parts[1].trim() ) ;
            } ) ;
        }
        else {
            log.error( "Config file not found." ) ;
        }
    }
    
    private String getKey( String indexName ) {
        if( translationMap.containsKey( indexName.toUpperCase() ) ) {
            return translationMap.get( indexName.toUpperCase() ) ;
        }
        return indexName ;
    }
}
