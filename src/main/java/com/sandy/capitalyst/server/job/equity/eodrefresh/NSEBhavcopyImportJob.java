package com.sandy.capitalyst.server.job.equity.eodrefresh;

import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.io.FileUtils ;
import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.BhavcopyImportResult ;
import com.sandy.capitalyst.server.api.equity.helper.bhavcopy.NSEBhavcopyImporter ;
import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo;
import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo.ReportMeta ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.capitalyst.server.core.util.StringUtil ;

import static com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo.* ;

@DisallowConcurrentExecution
public class NSEBhavcopyImportJob extends CapitalystJob {

    private static final Logger log = Logger.getLogger( NSEBhavcopyImportJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "ddMMMyyyy" ) ;
    public static final String KEY_LAST_IMPORT_DATE = "LAST_IMPORT_DATE" ;

    private final NSEBhavcopyDownloader downloader = new NSEBhavcopyDownloader() ;

    @Override
    public String executeJob( JobExecutionContext context, JobState state )
        throws Exception {

        log.debug( "Executing NSEBhavcopyRefreshJob" ) ;

        Date lastImportDate ;
        ReportMeta curBhavcopyMeta ;
        ReportMeta prevBhavcopyMeta ;
        BhavcopyImportResult importResult ;
        String successMessage = "Bhavcopy imported for " ;

        NSEReportsMetaRepo metaRepo = NSEReportsMetaRepo.instance();
        lastImportDate = getLastImportDate( state ) ;

        prevBhavcopyMeta = metaRepo.getPrevReportMeta( BHAVCOPY_META_KEY ) ;
        if( prevBhavcopyMeta == null ) {
            throw new Exception( "Current Bhavcopy meta is not available." ) ;
        }
        else {
            importResult = importBhavcopy( prevBhavcopyMeta, lastImportDate, state ) ;
            if( importResult != null ) {
                successMessage += "[" +
                        SDF.format( importResult.getBhavcopyDate() ) + ", " +
                        importResult.getNumRecordsImported() + " rows]" ;
            }
            else {
                successMessage += "[" + SDF.format( prevBhavcopyMeta.getTradingDate() ) + " @ latest ] "  ;
            }
        }

        curBhavcopyMeta  = metaRepo.getCurrentReportMeta( BHAVCOPY_META_KEY ) ;
        if( curBhavcopyMeta == null ) {
            throw new Exception( "Current Bhavcopy meta is not available." ) ;
        }
        else {
            importResult = importBhavcopy( curBhavcopyMeta,  lastImportDate, state ) ;
            if( importResult != null ) {
                successMessage += "[" +
                        SDF.format( importResult.getBhavcopyDate() ) + ", " +
                        importResult.getNumRecordsImported() + " rows]" ;
            }
            else {
                successMessage += "[" + SDF.format( curBhavcopyMeta.getTradingDate() ) + " @ latest]"  ;
            }
        }

        log.debug( "NSEBhavcopyRefreshJob completed execution." ) ;
        return successMessage ;
    }

    private Date getLastImportDate( JobState state ) throws Exception {

        Date lastImportDate ;
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

    private BhavcopyImportResult importBhavcopy( ReportMeta reportMeta, Date lastImportDate,
                                 JobState state )
            throws Exception {

        String bhavcopyContents ;
        NSEBhavcopyImporter importer ;
        BhavcopyImportResult result = null ;

        Date tradeDate = reportMeta.getTradingDate() ;
        String tradeDateStr = SDF.format( tradeDate ) ;

        if( tradeDate.after( lastImportDate ) ) {

            File file = downloader.downloadBhavcopy( reportMeta ) ;

            bhavcopyContents = FileUtils.readFileToString( file, "UTF-8" ) ;
            importer = new NSEBhavcopyImporter() ;

            // Note: job_run is updated by the framework based on any
            //       exception emitted from this method. Do no subdue
            //       exceptions.
            result = importer.importContents( bhavcopyContents ) ;
            updateJobState( state, result.getBhavcopyDate() ) ;

            log.debug( "\tBhavcopy for " + tradeDateStr + " imported."  ) ;
        }
        else {
            log.debug( "\tBhavcopy import for " + tradeDateStr + " skipped. " +
                       "Already imported." ) ;
        }

        return result ;
    }

    private void updateJobState( JobState state, Date importDate ) {
    
        String newImportDateVal = SDF.format( importDate ) ;
        state.setState( KEY_LAST_IMPORT_DATE, newImportDateVal ) ;
    }
}
