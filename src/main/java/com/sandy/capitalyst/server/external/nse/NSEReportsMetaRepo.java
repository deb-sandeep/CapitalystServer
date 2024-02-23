package com.sandy.capitalyst.server.external.nse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NSEReportsMetaRepo {

    private static final Logger log = Logger.getLogger( NSEReportsMetaRepo.class ) ;

    private static final String CM_REPORT_META_URL = "https://www.nseindia.com/api/daily-reports?key=CM" ;
    private static final String IDX_REPORT_META_URL = "https://www.nseindia.com/api/daily-reports?key=INDEX" ;
    private static final SimpleDateFormat REPORT_DATE_FMT = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    public static final String BHAVCOPY_META_KEY = "CM-BHAVCOPY-CSV" ;
    public static final String INDEX_META_KEY = "INDEX-SNAPSHOT" ;

    @Getter
    public static class ReportMeta {
        private String fileHashKey  = null ;
        private String fileKey      = null ;
        private String fileSegment  = null ;
        private String displayName  = null ;
        private String fileActlName = null ;
        private String filePath     = null ;
        private Date   tradingDate  = null ;
        private Double filePosition = null ;

        private String reportURL = null ;

        private ReportMeta( JsonNode node ) throws ParseException {

            fileHashKey = node.get( "fileHashKey" ).asText() ;
            fileKey     = node.get( "fileKey" ).asText() ;
            fileSegment = node.get( "fileSegment" ).asText() ;
            displayName = node.get( "displayName" ).asText() ;
            fileActlName= node.get( "fileActlName" ).asText() ;
            filePath    = node.get( "filePath" ).asText() ;
            tradingDate = REPORT_DATE_FMT.parse( node.get( "tradingDate" ).asText() ) ;
            filePosition= node.get( "filePosition" ).asDouble() ;

            reportURL = filePath + fileActlName ;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ReportMeta{ " );
            sb.append("  fileHashKey='" ).append(fileHashKey ).append('\'');
            sb.append(", fileKey='"     ).append(fileKey     ).append('\'');
            sb.append(", fileSegment='" ).append(fileSegment ).append('\'');
            sb.append(", displayName='" ).append(displayName ).append('\'');
            sb.append(", fileActName='" ).append(fileActlName).append('\'');
            sb.append(", filePath='"    ).append(filePath    ).append('\'');
            sb.append(", tradingDate="  ).append(tradingDate );
            sb.append(", filePosition=" ).append(filePosition);
            sb.append('}') ;
            return sb.toString();
        }
    }

    private Date curDate = null ;
    private Date prevDate = null ;
    private Date lastLoadTime = null ;

    private final Map<String, ReportMeta> curMetaMap = new HashMap<>() ;
    private final Map<String, ReportMeta> prevMetaMap = new HashMap<>() ;

    private final HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;

    private static final NSEReportsMetaRepo instance = new NSEReportsMetaRepo() ;

    public static NSEReportsMetaRepo instance() throws Exception {
        if( instance.reloadRequired() ) {
            instance.loadMeta() ;
        }
        return instance ;
    }

    private NSEReportsMetaRepo() {}

    private boolean reloadRequired() {

        boolean reloadRequired = false ;
        if( lastLoadTime == null ) {
            reloadRequired = true ;
        }
        else {
            // Cache window is 5 minutes.
            Date curTime = new Date() ;
            reloadRequired = ( curTime.getTime() - lastLoadTime.getTime() ) > 300 * 1000 ;
        }
        return  reloadRequired ;
    }

    private void loadMeta() throws Exception {
        log.info( "Loading CM reports meta." ) ;
        loadMeta( CM_REPORT_META_URL ) ;

        log.info( "Loading Index reports meta." ) ;
        loadMeta( IDX_REPORT_META_URL ) ;

        lastLoadTime = new Date() ;
    }

    private void loadMeta( String url ) throws Exception {

        String jsonStr = downloader.getResource( url, "nse-reports-headers.txt" ) ;

        ObjectMapper objMapper = new ObjectMapper() ;
        JsonNode jsonRoot  = objMapper.readTree( jsonStr ) ;

        curDate  = loadMeta( "currentDate",  curMetaMap,  jsonRoot ) ;
        prevDate = loadMeta( "previousDate", prevMetaMap, jsonRoot ) ;
    }

    private Date loadMeta( String dateKey, Map<String, ReportMeta> metaMap,
                           JsonNode rootNode )
            throws Exception {

        Date date = REPORT_DATE_FMT.parse( rootNode.get( dateKey ).asText() ) ;
        log.info( "-> Date = " + REPORT_DATE_FMT.format( date ) ) ;

        String metaArrayKey = dateKey.equals( "currentDate" ) ? "CurrentDay" : "PreviousDay" ;
        JsonNode dataNode  = rootNode.get( metaArrayKey ) ;

        for( int i=1; i<dataNode.size(); i++ ) {

            JsonNode jsonNode = dataNode.get( i ) ;
            ReportMeta meta = new ReportMeta( jsonNode ) ;
            metaMap.put( meta.getFileKey(), meta ) ;
            log.debug( "-> Meta = " + meta ) ;
        }
        return date ;
    }

    public ReportMeta getCurrentReportMeta( String reportKey ) {
        return curMetaMap.get( reportKey ) ;
    }

    public ReportMeta getPrevReportMeta( String reportKey ) {
        return prevMetaMap.get( reportKey ) ;
    }
}
