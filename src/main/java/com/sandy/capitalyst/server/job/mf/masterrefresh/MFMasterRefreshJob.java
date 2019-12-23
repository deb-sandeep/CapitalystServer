package com.sandy.capitalyst.server.job.mf.masterrefresh;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.jsoup.Jsoup ;
import org.jsoup.nodes.Document ;
import org.jsoup.nodes.Element ;
import org.jsoup.select.Elements ;
import org.quartz.DisallowConcurrentExecution ;
import org.quartz.JobExecutionContext ;

import com.sandy.capitalyst.server.CapitalystServer ;
import com.sandy.capitalyst.server.core.network.HTTPResourceDownloader ;
import com.sandy.capitalyst.server.core.scheduler.CapitalystJob ;
import com.sandy.capitalyst.server.core.scheduler.JobState ;
import com.sandy.capitalyst.server.dao.mf.MutualFund ;
import com.sandy.capitalyst.server.dao.mf.MutualFundRepo ;
import com.sandy.capitalyst.server.util.StringUtil ;

@DisallowConcurrentExecution
public class MFMasterRefreshJob extends CapitalystJob {
    
    private static final Logger log = Logger.getLogger( MFMasterRefreshJob.class ) ;
    
    private static final String URL = "https://nsdl.co.in/mutual-fund-popup.html" ;
    
    private MutualFundRepo mfRepo = null ;
    
    @Override
    protected void preExecute( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        this.mfRepo = CapitalystServer.getBean( MutualFundRepo.class ) ;
    }

    @Override
    protected void executeJob( JobExecutionContext context,
                               JobState state ) 
        throws Exception {
        
        log.debug( "Executing MFMasterRefreshJob" ) ;
        String siteContents = getNSDLSiteContent() ;
        List<String> parsedContents = parseNSDLContents( siteContents ) ;
        
        String lastFundName = null ;
        
        for( int i=0; i<parsedContents.size(); i+=4 ) {
            String fundName = parsedContents.get( i ) ;
            String isin = parsedContents.get( i+1 ) ;
            String description = parsedContents.get( i+2 ) ;
            
            if( StringUtil.isNotEmptyOrNull( fundName ) ) {
                lastFundName = fundName ;
            }
            else {
                fundName = lastFundName ;
            }
            updateMaster( fundName, isin, description ) ;
        }
    }
    
    private String getNSDLSiteContent() throws Exception {
        HTTPResourceDownloader downloader = HTTPResourceDownloader.instance() ;
        String contents = downloader.getResource( URL, "nsdl-mf.txt" ) ;
        return contents ;
//        String filePath = "/Users/sandeep/temp/nsdl.txt" ;
//        String contents = FileUtils.readFileToString( new File( filePath ) ) ;
//        return contents ;
    }
    
    private List<String> parseNSDLContents( String contents ) throws Exception {
        
        List<String> parsedContents = new ArrayList<>() ;
        Document doc = Jsoup.parse( contents ) ;
        Elements elements = doc.select( "td[class^=tablecontent]" ) ;
        
        for( Element element : elements ) {
            String text = element.text() ;
            parsedContents.add( text.trim() ) ;
        }
        return parsedContents ;
    }
    
    private void updateMaster( String fundName, String isin, String description ) {
        
        MutualFund fund = mfRepo.findByIsin( isin ) ;
        if( fund == null ) {
            fund = new MutualFund() ;
            fund.setIsin( isin ) ;
            fund.setFundName( fundName ) ;
            fund.setDescription( description ) ;
            log.debug( "Saving new = " + isin ) ;
            mfRepo.save( fund ) ;
        }
        else {
            boolean dirty = false ;
            if( !fund.getFundName().equals( fundName ) ) {
                fund.setFundName( fundName ) ;
                dirty = true ;
            }
            
            if( !fund.getDescription().equals( description ) ) {
                fund.setDescription( description ) ;
                dirty = true ;
            }
            
            if( dirty ) {
                log.debug( "Updating = " + isin ) ;
                mfRepo.save( fund ) ;
            }
        }
    }
}
