package com.sandy.capitalyst.server.test.standalone;

import com.sandy.capitalyst.server.api.equity.helper.EquityHistDataImporter;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EquityHistDataImporterExecutor {

    private static final Logger log = Logger.getLogger( EquityHistDataImporterExecutor.class ) ;

    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MM-yyyy" ) ;

    public static void main( String[] args ) throws Exception {
        CapitalystStandaloneDriver driver = null ;
        try {
            driver = new CapitalystStandaloneDriver() ;
            driver.initializeDriver( args ) ;

            EquityHistDataImporterExecutor executor = new EquityHistDataImporterExecutor() ;
            executor.execute( driver ) ;
        }
        catch( Throwable t ) {
            log.error( "Error encountered.", t );
        }
        finally {
            if( driver != null ) {
                driver.shutdownDriver() ;
            }
        }
    }

    public void execute( CapitalystStandaloneDriver driver ) throws Exception {

        EquityHistDataImporter importer ;
        HistoricEQDataRepo repo = driver.getBean( HistoricEQDataRepo.class ) ;

        String symbol = "APOLLOTYRE" ;
        Date fromDate = SDF.parse( "02-11-2023" ) ;
        Date toDate = SDF.parse( "31-12-2023" ) ;

        importer = new EquityHistDataImporter( repo ) ;
        importer.importFromServer( symbol, fromDate, toDate ) ;
    }
}
