package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.io.FileUtils ;
import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.BhavcopyImportResult ;
import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.NSEBhavcopyImporter ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.common.util.StringUtil ;

@DisallowConcurrentExecution
public class NSEBhavcopyImportJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( NSEBhavcopyImportJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "ddMMMyyyy" ) ;
    private static final String KEY_LAST_IMPORT_DATE = "LAST_IMPORT_DATE" ;
    
    private NSEBhavcopyDownloader downloader = new NSEBhavcopyDownloader() ;

    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        String bhavcopyContents = null ;
        NSEBhavcopyImporter importer = null ;
        BhavcopyImportResult result = null ;
        
        log.debug( "NSEBhavcopyRefreshJob executeJob" ) ;

        Date lastImportDate = getLastImportDate( state ) ;
        
        if( bhavcopyNeedsImporting( lastImportDate ) ) {
            
            bhavcopyContents = getBhavcopyContents() ;
            
            importer = new NSEBhavcopyImporter() ;
            result = importer.importContents( bhavcopyContents ) ;
            
            updateJobState( state, result ) ;
            log.debug( "\tBhavcopy imported."  ) ;
        }
        else {
            log.debug( "\tBhavcopy import skipped. Already imported." ) ;
        }
    }
    
    private Date getLastImportDate( JobState state ) throws Exception {
        
        Date lastImportDate = null ;
        String lastImportDateVal = state.getStateAsString( KEY_LAST_IMPORT_DATE ) ;
     
        if( StringUtil.isNotEmptyOrNull( lastImportDateVal ) ) {
            lastImportDate = SDF.parse( lastImportDateVal ) ;
        }
        else {
            Date today = DateUtils.truncate( new Date(), Calendar.DAY_OF_MONTH ) ;
            lastImportDate = DateUtils.addDays( today, -1 ) ;
            log.debug( "\tLast imported date not found in job state." ) ; 
            log.debug( "\t Setting to a day in the past." ) ;
        }
        
        log.debug( "\tLast imported date - " + lastImportDateVal ) ;
        return lastImportDate ;
    }

    private boolean bhavcopyNeedsImporting( Date lastImportDate ) 
            throws Exception {
        
        Date latestAvailableBhavcopyDate = downloader.getLatestBhavcopyDate() ;
        return latestAvailableBhavcopyDate.after( lastImportDate ) ;
    }
    
    private String getBhavcopyContents() throws Exception {
        
        File file = downloader.downloadBhavcopy() ;
        String contents = FileUtils.readFileToString( file ) ;
        return contents ;
    }

    private void updateJobState( JobState state, 
                                 BhavcopyImportResult importResult ) {
    
        String newImportDateVal = SDF.format( importResult.getBhavcopyDate() ) ;
        state.setState( KEY_LAST_IMPORT_DATE, newImportDateVal ) ;
    }
}