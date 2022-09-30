package com.sandy.capitalyst.server.api.equity ;

import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityGraphDataController {

    private static final Logger log = Logger.getLogger( EquityGraphDataController.class ) ;
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private HistoricEQDataRepo hedRepo = null ;
    
    @GetMapping( "/Equity/GraphData" ) 
    public ResponseEntity<GraphData> getGraphData( 
                  @RequestParam( "duration" ) String durationKey,
                  @RequestParam( name="symbolNse", required=true ) String symbolNse ) {
        
        try {
            EquityMaster em = emRepo.findBySymbol( symbolNse ) ;
            
            if( StringUtil.isEmptyOrNull( durationKey ) ) {
                durationKey = "6m" ;
            }
            
            Date toDate = new Date() ;
            Date fromDate = getFromDate( toDate, durationKey ) ;
            
            GraphData graphData = constructGraphData( fromDate, toDate, em ) ;
            
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
        
        Date fromDate = null ;
        switch( key ) {
            case "1m":
                fromDate = DateUtils.addMonths( toDate, -1 ) ;
                break ;
            case "2m":
                fromDate = DateUtils.addMonths( toDate, -2 ) ;
                break ;
            case "3m":
                fromDate = DateUtils.addMonths( toDate, -3 ) ;
                break ;
            case "6m":
                fromDate = DateUtils.addMonths( toDate, -6 ) ;
                break ;
            case "1y":
                fromDate = DateUtils.addYears( toDate, -1 ) ;
                break ;
            case "2y":
                fromDate = DateUtils.addYears( toDate, -2 ) ;
                break ;
            case "3y":
                fromDate = DateUtils.addYears( toDate, -3 ) ;
                break ;
            default:
                fromDate = DateUtils.addMonths( toDate, -6 ) ;
                break ;
        }
        return fromDate ;
    }
    
    private GraphData constructGraphData( Date fromDate, Date toDate, 
                                          EquityMaster em ) {
        
        GraphData graphData = new GraphData() ;
        
        
        return graphData ;
    }
}


























