package com.sandy.capitalyst.server.api.equity.graph ;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR ;
import static org.springframework.http.HttpStatus.OK ;
import static org.springframework.http.ResponseEntity.status ;

import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.commons.lang3.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.graph.internal.BarSeriesCache ;
import com.sandy.capitalyst.server.api.equity.graph.internal.EquityGraphDataBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;

@RestController
public class EquityGraphDataController {

    private static final Logger log = Logger.getLogger( EquityGraphDataController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
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
            
            EquityGraphDataBuilder builder   = null ;
            GraphData              graphData = null ;
            
            builder = new EquityGraphDataBuilder() ;
            graphData = builder.constructGraphData( fromDate, toDate, 
                                                    em, ownerName ) ;
            
            BarSeriesCache.instance().put( symbolNse, builder.getBarSeries() ) ;
            
            return status( OK ).body( graphData ) ;
        }
        catch( Exception e ) {
            log.error( "Error :: Getting equity portfolio.", e ) ;
            return status( INTERNAL_SERVER_ERROR ).body( null ) ;
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
}
