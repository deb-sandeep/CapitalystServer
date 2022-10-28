package com.sandy.capitalyst.server.job.equity.idxrefresh;

import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.common.util.StringUtil ;

@DisallowConcurrentExecution
public class NSEIndexEODImportJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( NSEIndexEODImportJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "ddMMMyyyy" ) ;
    private static final String KEY_LAST_IMPORT_DATE = "LAST_IMPORT_DATE" ;
    
    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "NSEIndexEODImportJob executeJob" ) ;
        
        Date now = new Date() ;
        Date lastImportDate = getLastImportDate( state ) ;
        Date nextImportDate = DateUtils.addDays( lastImportDate, 1 ) ;
        
        NSEIndexEODImporter importer = null ;
        
        while( nextImportDate.before( now ) ) {
            
            importer = new NSEIndexEODImporter( nextImportDate ) ;
            int numRecordsImported = importer.importEODValues() ;
            
            if( numRecordsImported > 0 ) {
                
                state.setState( KEY_LAST_IMPORT_DATE, 
                                SDF.format( nextImportDate ) ) ;
                
                log.debug( "Index EOD values imported for date " + 
                           SDF.format( nextImportDate ) ) ;
            }
            else {
                log.debug( "No Index EOD file for " + 
                           SDF.format( nextImportDate ) ) ;
            }
            nextImportDate = DateUtils.addDays( nextImportDate, 1 ) ;
        }
    }
    
    private Date getLastImportDate( JobState state ) throws Exception {
        
        Date lastImportDate = null ;
        String lastImportDateVal = state.getStateAsString( KEY_LAST_IMPORT_DATE ) ;
        
        if( StringUtil.isNotEmptyOrNull( lastImportDateVal ) ) {
            lastImportDate = SDF.parse( lastImportDateVal ) ;
        }
        else {
            lastImportDate = SDF.parse( "10Oct2022" ) ;
        }
        
        log.debug( "\tLast imported date - " + lastImportDateVal ) ;
        return lastImportDate ;
    }
}