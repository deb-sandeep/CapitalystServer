package com.sandy.capitalyst.server.api.ota.action.idxhistupdate;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.index.helper.IndexHistDataPartImporter ;
import com.sandy.capitalyst.server.api.ota.action.OTA ;

public class HistoricIdxDataImporterOTA extends OTA {
    
    private static final Logger log = Logger.getLogger( HistoricIdxDataImporterOTA.class ) ;
    
    public static final String NAME = "HistoricIdxDataImporter" ;
    
    public HistoricIdxDataImporterOTA() {
        super( NAME ) ;
    }
    
    @Override
    protected void execute() throws Exception {

        IndexHistDataPartImporter partImporter = null ;
        
        try {
            partImporter = new IndexHistDataPartImporter() ;
            partImporter.execute() ;
        }
        catch( Exception e ) {
            log.error( "Exception", e ) ;
        }
    }
}
