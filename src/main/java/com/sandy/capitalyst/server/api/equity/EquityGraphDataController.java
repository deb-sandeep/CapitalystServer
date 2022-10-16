package com.sandy.capitalyst.server.api.equity ;

import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityGraphDataBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityGraphDataController {

    private static final Logger log = Logger.getLogger( EquityGraphDataController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private HistoricEQDataRepo hedRepo = null ;
    
    @GetMapping( "/Equity/GraphData" ) 
    public ResponseEntity<GraphData> getGraphData( 
                  @RequestParam( "duration" ) String durationKey,
                  @RequestParam( name="symbolNse", required=true ) String symbolNse,
                  @RequestParam( name="owner",     required=true ) String ownerName ) {
        try {
            
            EquityMaster em = emRepo.findBySymbol( symbolNse ) ;
            
            if( StringUtil.isEmptyOrNull( durationKey ) ) {
                durationKey = "6m" ;
            }
            
            Date toDate = new Date() ;
            Date fromDate = getFromDate( toDate, durationKey ) ;
            
            log.debug( "Fetching graph data" ) ;
            log.debug( "   Duration = " + durationKey ) ;
            log.debug( "   Symbol   = " + symbolNse   ) ;
            log.debug( "   Owner    = " + ownerName   ) ;
            log.debug( "   From Date= " + SDF.format( fromDate ) ) ;
            log.debug( "   To Date  = " + SDF.format( toDate ) ) ;
            
            List<HistoricEQData>   histData  = null ;
            EquityGraphDataBuilder builder   = null ;
            GraphData              graphData = null ;
            
            histData = loadHistoricData( symbolNse, fromDate, toDate ) ;
            builder = new EquityGraphDataBuilder( histData ) ;
            graphData = builder.constructGraphData( 
                                             fromDate, toDate, em, ownerName ) ;
            
            return ResponseEntity.status( HttpStatus.OK )
                                 .body( graphData ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                                 .body( null ) ;
        }
    }
    
    private Date getFromDate( final Date toDate, final String key ) {
        
        Date   fromDate = null ;
        int    amount   = Integer.parseInt( key.substring( 0, 1 ) ) ;
        String duration = key.substring( 1 ) ;
        
        if( duration.equalsIgnoreCase( "m" ) ) {
            fromDate = DateUtils.addMonths( toDate, -1*amount ) ;
        }
        else if( duration.equalsIgnoreCase( "y" ) ) {
            fromDate = DateUtils.addYears( toDate, -1*amount ) ;
        }
        return fromDate ;
    }
    
    private List<HistoricEQData> loadHistoricData( 
                                   String symbol, Date fromDate, Date toDate ) {

        List<HistoricEQData> histDataList = null ;
        histDataList = hedRepo.getHistoricData( symbol, fromDate, toDate ) ;
        return histDataList ;
    }

    
}
