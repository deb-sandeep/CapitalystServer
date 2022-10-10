package com.sandy.capitalyst.server.api.ota.action.eqhistupdate;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataPartImporter ;
import com.sandy.capitalyst.server.api.ota.action.OTA ;

public class HistoricEQDataImporterOTA extends OTA {
    
    private static final Logger log = Logger.getLogger( HistoricEQDataImporterOTA.class ) ;
    
    public static final String NAME = "HistoricEQDataImporter" ;
    
    public HistoricEQDataImporterOTA() {
        super( NAME ) ;
    }
    
    @Override
    protected void execute() throws Exception {

        EquityHistDataPartImporter partImporter = null ;
        
        try {
            partImporter = new EquityHistDataPartImporter() ;
            partImporter.execute() ;
        }
        catch( Exception e ) {
            log.error( "Exception", e ) ;
        }
    }
}
