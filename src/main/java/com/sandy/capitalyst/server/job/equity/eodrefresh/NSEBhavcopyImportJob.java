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
import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo;
import com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo.ReportMeta ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.common.util.StringUtil ;

import static com.sandy.capitalyst.server.external.nse.NSEReportsMetaRepo.* ;

@DisallowConcurrentExecution
public class NSEBhavcopyImportJob extends CapitalystJob {

    private static final Logger log = Logger.getLogger( NSEBhavcopyImportJob.class ) ;
    
    public static final SimpleDateFormat SDF = new SimpleDateFormat( "ddMMMyyyy" ) ;
    public static final String KEY_LAST_IMPORT_DATE = "LAST_IMPORT_DATE" ;

    private NSEBhavcopyDownloader downloader = new NSEBhavcopyDownloader() ;
    private NSEReportsMetaRepo metaRepo = null ;

    @Override
    public void executeJob( JobExecutionContext context, JobState state )
        throws Exception {

        log.debug( "Executing NSEBhavcopyRefreshJob" ) ;

        Date lastImportDate = null ;
        ReportMeta curBhavcopyMeta = null ;
        ReportMeta prevBhavcopyMeta = null ;

        metaRepo = NSEReportsMetaRepo.instance() ;

        curBhavcopyMeta  = metaRepo.getCurrentReportMeta( BHAVCOPY_META_KEY ) ;
        prevBhavcopyMeta = metaRepo.getPrevReportMeta( BHAVCOPY_META_KEY ) ;
        lastImportDate = getLastImportDate( state ) ;

        importBhavcopy( prevBhavcopyMeta, lastImportDate, state ) ;
        importBhavcopy( curBhavcopyMeta,  lastImportDate, state ) ;

        log.debug( "NSEBhavcopyRefreshJob completed execution." ) ;
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

    private void importBhavcopy( ReportMeta reportMeta, Date lastImportDate,
                                 JobState state )
            throws Exception {

        String bhavcopyContents = null ;
        NSEBhavcopyImporter importer = null ;
        BhavcopyImportResult result = null ;

        Date tradeDate = reportMeta.getTradingDate() ;
        String tradeDateStr = SDF.format( tradeDate ) ;

        if( tradeDate.after( lastImportDate ) ) {

            File file = downloader.downloadBhavcopy( reportMeta ) ;

            bhavcopyContents = FileUtils.readFileToString( file ) ;
            importer = new NSEBhavcopyImporter() ;

            result = importer.importContents( bhavcopyContents ) ;

            updateJobState( state, result ) ;

            log.debug( "\tBhavcopy for " + tradeDateStr + " imported."  ) ;
        }
        else {
            log.debug( "\tBhavcopy import for " + tradeDateStr + " skipped. " +
                       "Already imported." ) ;
        }
    }

    private void updateJobState( JobState state,
                                 BhavcopyImportResult importResult ) {
    
        String newImportDateVal = SDF.format( importResult.getBhavcopyDate() ) ;
        state.setState( KEY_LAST_IMPORT_DATE, newImportDateVal ) ;
    }
}