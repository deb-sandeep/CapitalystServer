package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.common.util.StringUtil ;

@DisallowConcurrentExecution
public class NSEBhavcopyImportJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( NSEBhavcopyImportJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "ddMMMyyyy" ) ;
    private static final String KEY_LAST_IMPORT_DATE = "LAST_IMPORT_DATE" ;
    
    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "NSEBhavcopyRefreshJob executeJob" ) ;
        String lastImportDateVal = state.getStateAsString( KEY_LAST_IMPORT_DATE ) ;
        Date lastImportDate = null ;
        if( StringUtil.isNotEmptyOrNull( lastImportDateVal ) ) {
            lastImportDate = SDF.parse( lastImportDateVal ) ;
            log.debug( "\tLast imported date - " + lastImportDateVal ) ;
        }
        
        NSEBhavcopyImporter importer = new NSEBhavcopyImporter() ;
        lastImportDate = importer.importBhavcopy( lastImportDate ) ;
        
        String newImportDateVal = SDF.format( lastImportDate ) ;
        state.setState( KEY_LAST_IMPORT_DATE, newImportDateVal ) ;
        
        if( !newImportDateVal.equals( lastImportDateVal ) ) {
            log.debug( "\tBhavcopy imported."  ) ;
        }
        else {
            log.debug( "\tBhavcopy import skipped. Already imported." ) ;
        }
    }
}