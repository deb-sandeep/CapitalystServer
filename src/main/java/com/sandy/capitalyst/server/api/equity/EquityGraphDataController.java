package com.sandy.capitalyst.server.api.equity ;

import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.http.HttpStatus ;
import org.springframework.http.ResponseEntity ;
import org.springframework.web.bind.annotation.GetMapping ;
import org.springframework.web.bind.annotation.RequestParam ;
import org.springframework.web.bind.annotation.RestController ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.FamilyEquityHoldingVO ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData.DayPriceData ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData.TradeData ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.core.util.StringUtil ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTrade ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityMasterRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTradeRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

@RestController
public class EquityGraphDataController {

    private static final Logger log = Logger.getLogger( EquityGraphDataController.class ) ;
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;
    
    private class DataHolder {
        HistoricEQData histData  = null ;
        EquityTrade    buyTrade  = null ;
        EquityTrade    sellTrade = null ;
    }
    
    @Autowired
    private EquityMasterRepo emRepo = null ;
    
    @Autowired
    private HistoricEQDataRepo hedRepo = null ;
    
    @Autowired
    private EquityTradeRepo etRepo = null ;
    
    @Autowired
    private EquityTxnRepo etxnRepo = null ;
    
    @Autowired
    private EquityHoldingRepo ehRepo = null ;
    
    @Autowired
    private EquityIndicatorsRepo eiRepo = null ;
    
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
            
            GraphData graphData = constructGraphData( fromDate, toDate, 
                                                      em, ownerName ) ;
            
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
                                          EquityMaster em, 
                                          String ownerName ) {
        
        GraphData graphData = new GraphData() ;
        Map<Date, DataHolder> data = new TreeMap<>() ;
        
        loadEODData( data, fromDate, toDate, em ) ;
        populateTradeData( data, em.getSymbolIcici(), ownerName ) ;
        
        populateLabels      ( graphData, data ) ;
        populateEoDPriceList( graphData, data ) ;
        populateBuySellData ( graphData, data ) ;
        populateAvgCostData ( graphData, em.getSymbolIcici(), ownerName ) ;
        populateCMPData     ( graphData ) ;
        
        EquityIndicators ei = eiRepo.findByIsin( em.getIsin() ) ;
        graphData.setIndicators( ei ) ;
        graphData.setEquityMaster( em ) ;
        
        return graphData ;
    }

    private void populateLabels( GraphData graphData,
                                 Map<Date, DataHolder> data ) {
        
        List<Long> labels = graphData.getLabels() ;
        data.forEach( (date,holding)->labels.add( date.getTime() ) ) ;
    }

    private void populateTradeData( Map<Date, DataHolder> data,
                                    String symbolIcici, String ownerName ) {
        
        if( ownerName.equals( "Family" ) ) {
            Breeze.instance().getAllCreds().forEach( cred -> {
                populateTradeData( data, symbolIcici, cred.getUserName() ) ;
            } ) ;
        }
        else {
            List<EquityTrade> trades = etRepo.findTrades( symbolIcici, ownerName ) ;
            trades.forEach( trade -> {
                
                if( data.containsKey( trade.getTradeDate() ) ) {
                    
                    DataHolder holder = data.get( trade.getTradeDate() ) ;
                    
                    if( trade.getAction().equals( "Buy" ) ) {
                        holder.buyTrade = trade ;
                    }
                    else {
                        holder.sellTrade = trade ;
                    }
                }
            } ) ;
        }

    }

    private void populateEoDPriceList( GraphData graphData,
                                       Map<Date, DataHolder> data ) {
    
        List<Float> eodPriceList = graphData.getEodPriceList() ;
        data.forEach( (date,holding) -> 
            eodPriceList.add( holding.histData.getClose() ) 
        ) ;
    }

    private void populateBuySellData( GraphData graphData,
                                      Map<Date, DataHolder> data ) {
        
        data.forEach( (date,holding) -> { 
            
            if( !(holding.buyTrade == null && holding.sellTrade == null) ) {
                
                EquityTrade et = holding.buyTrade != null ? holding.buyTrade : 
                                                            holding.sellTrade ;

                TradeData t = new TradeData() ;
                
                t.setX( et.getTradeDate().getTime() ) ;
                t.setY( et.getValueAtCost() / et.getQuantity() ) ;
                t.setQ( et.getQuantity() ) ;
                
                if( et.getAction().equals( "Buy" ) ) {
                    graphData.getBuyData().add( t ) ;
                }
                else {
                    graphData.getSellData().add( t ) ;
                }
            }
        } ) ;
    }

    private void populateAvgCostData( GraphData graphData,
                                      String symbolIcici,
                                      String ownerName ) {
        
        float avgPrice = getAvgCost( ownerName, symbolIcici, graphData ) ;
        if( avgPrice > 0 ) {
            
            List<Long> labels = graphData.getLabels() ;
            
            DayPriceData s = new DayPriceData() ;
            DayPriceData e = new DayPriceData() ;
            
            s.setX( labels.get( 0 ) ) ;
            e.setX( labels.get( labels.size()-1 ) ) ;
            
            e.setY( avgPrice ) ;
            s.setY( avgPrice ) ;
            
            graphData.getAvgData().add( s ) ;
            graphData.getAvgData().add( e ) ;
        }
    }
    
    private Map<Date, DataHolder> loadEODData( Map<Date, DataHolder> data,
                                               Date fromDate, Date toDate,
                                               EquityMaster em ) {
        
        List<HistoricEQData> histEqDataList = null ;
        
        histEqDataList = hedRepo.getHistoricData( em.getSymbol(), 
                                                  fromDate, toDate ) ;
        histEqDataList.forEach( item -> {
            DataHolder holder = new DataHolder() ;
            holder.histData = item ;
            data.put( item.getDate(), holder ) ;
        } ) ;
        
        populateLatestCMP( data, em.getSymbol() ) ;
        
        return data ;
    }
    
    // If we are tracking intra-day price for this stock, there is a possibility
    // that we might have a more recent market price. If so, lets add it to the
    // graph data.
    private void populateLatestCMP( Map<Date, DataHolder> data, String symbolNse ) {
        
        List<EquityHolding> holdings = null ;
        EquityHolding holding = null ;
        
        holdings = ehRepo.findBySymbolNse( symbolNse ) ;
        
        if( holdings != null && !holdings.isEmpty() ) {
            holding = holdings.get( 0 ) ;
            
            Date lastUpdate = holding.getLastUpdate() ;
            lastUpdate = DateUtils.truncate( lastUpdate, Calendar.DAY_OF_MONTH ) ;
            
            if( !data.containsKey( lastUpdate ) ) {
                HistoricEQData histData = new HistoricEQData() ;
                histData.setClose( holding.getCurrentMktPrice() ) ;
                histData.setDate( lastUpdate ) ;
                
                DataHolder holder = new DataHolder() ;
                holder.histData = histData ;
                data.put( lastUpdate, holder ) ;
            }
        }
    }
    
    private float getAvgCost( String ownerName, String symbolIcici, 
                              GraphData graphData ) {
        
        List<EquityTxn>           txns      = null ;
        IndividualEquityHoldingVO indVO     = null ;
        FamilyEquityHoldingVO     famVO     = null ;
        EquityHolding             holding   = null ;
        EquityHoldingVOBuilder    voBuilder = new EquityHoldingVOBuilder() ;
        
        if( ownerName.equals( "Family" ) ) {
            
            for( BreezeCred cred : Breeze.instance().getAllCreds() ) {
                
                holding = ehRepo.findByOwnerNameAndSymbolIcici( 
                                             cred.getUserName(), symbolIcici ) ;
                
                if( holding != null ) {
                    
                    txns = etxnRepo.findByHoldingIdOrderByTxnDateAscActionAsc( 
                                                             holding.getId() ) ;
                    indVO = voBuilder.buildVO( holding, txns ) ;
                    
                    if( famVO == null ) {
                        famVO = new FamilyEquityHoldingVO( indVO ) ;
                    }
                    else {
                        famVO.addIndividualHoldingVO( indVO ) ;
                    }
                }
            }
            
            graphData.setHolding( famVO ) ;
            return famVO != null ? famVO.getAvgCostPrice() : 0 ;
        }
        else {
            holding = ehRepo.findByOwnerNameAndSymbolIcici( ownerName, 
                                                            symbolIcici ) ;
            if( holding != null ) {
                txns = etxnRepo.findByHoldingIdOrderByTxnDateAscActionAsc( 
                        holding.getId() ) ;
                indVO = voBuilder.buildVO( holding, txns ) ;
            }

            graphData.setHolding( indVO ) ;
            return indVO != null ? indVO.getAvgCostPrice() : 0 ;
        }
    }
    
    private void populateCMPData( GraphData graphData ) {
        
        List<Float> eodList = graphData.getEodPriceList() ;
        List<Long>  labels  = graphData.getLabels() ;
        
        float cmp     = eodList.get( eodList.size()-1 ) ;
        long  lastDay = labels.get( labels.size()-1 ) ;
        
        DayPriceData d = new DayPriceData() ;
        d.setX( lastDay ) ;
        d.setY( cmp ) ;
        
        graphData.getCmpData().add( d ) ;
    }
}


























