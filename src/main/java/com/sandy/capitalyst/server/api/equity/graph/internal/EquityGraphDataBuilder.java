package com.sandy.capitalyst.server.api.equity.graph.internal ;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.ta4j.core.BarSeries ;
import org.ta4j.core.BaseBarSeries ;

import com.sandy.capitalyst.server.api.equity.helper.EquityHoldingVOBuilder ;
import com.sandy.capitalyst.server.api.equity.vo.FamilyEquityHoldingVO ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData ;
import com.sandy.capitalyst.server.api.equity.vo.GraphData.DayPriceData ;
import com.sandy.capitalyst.server.api.equity.vo.IndividualEquityHoldingVO ;
import com.sandy.capitalyst.server.breeze.Breeze ;
import com.sandy.capitalyst.server.breeze.BreezeCred ;
import com.sandy.capitalyst.server.dao.equity.EquityHolding ;
import com.sandy.capitalyst.server.dao.equity.EquityIndicators ;
import com.sandy.capitalyst.server.dao.equity.EquityMaster ;
import com.sandy.capitalyst.server.dao.equity.EquityTrade ;
import com.sandy.capitalyst.server.dao.equity.EquityTxn ;
import com.sandy.capitalyst.server.dao.equity.HistoricEQData ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityHoldingRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityIndicatorsRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTradeRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.EquityTxnRepo ;
import com.sandy.capitalyst.server.dao.equity.repo.HistoricEQDataRepo ;

public class EquityGraphDataBuilder {

    static final Logger log = Logger.getLogger( EquityGraphDataBuilder.class ) ;
    
    private class DataHolder {
        HistoricEQData    histData = null ;
        List<EquityTrade> trades   = new ArrayList<>() ;
    }
    
    private EquityTradeRepo      etRepo   = null ;
    private EquityTxnRepo        etxnRepo = null ;
    private EquityHoldingRepo    ehRepo   = null ;
    private EquityIndicatorsRepo eiRepo   = null ;
    private HistoricEQDataRepo   hedRepo  = null ;
    
    private BarSeries barSeries = null ;
    
    public EquityGraphDataBuilder() {
        
        etRepo   = getBean( EquityTradeRepo.class      ) ;
        etxnRepo = getBean( EquityTxnRepo.class        ) ;
        ehRepo   = getBean( EquityHoldingRepo.class    ) ;
        eiRepo   = getBean( EquityIndicatorsRepo.class ) ;
        hedRepo  = getBean( HistoricEQDataRepo.class   ) ;
    }
    
    public GraphData constructGraphData( Date fromDate, Date toDate, 
                                         EquityMaster em, 
                                         String ownerName ) {
        
        GraphData graphData = new GraphData() ;
        Map<Date, DataHolder> data = new TreeMap<>() ;
        
        loadEODData( data, fromDate, toDate, em.getSymbol() ) ;
        populateTradeData( data, em.getSymbolIcici(), ownerName ) ;
        
        populateLabels      ( graphData, data ) ;
        populateEoDPriceList( graphData, data ) ;
        populateBuySellData ( graphData, data ) ;
        populateAvgCostData ( graphData, em.getSymbolIcici(), ownerName ) ;
        populateCMPData     ( graphData ) ;
        populateBarSeries   ( em.getSymbol(), data ) ;
        
        EquityIndicators ei = eiRepo.findByIsin( em.getIsin() ) ;
        graphData.setIndicators( ei ) ;
        graphData.setEquityMaster( em ) ;
        
        return graphData ;
    }
    
    public BarSeries getBarSeries() {
        return this.barSeries ;
    }
    
    private void populateBarSeries( String symbolNse, 
                                    Map<Date, DataHolder> data ) {
        
        barSeries = new BaseBarSeries( symbolNse ) ;
        data.forEach( (date, holder) -> {
            barSeries.addBar( holder.histData.toBar() ) ;
        } ) ;
    }

    private Map<Date, DataHolder> loadEODData( Map<Date, DataHolder> data,
                                               Date fromDate, Date toDate,
                                               String symbolNse ) {
        
        List<HistoricEQData> histData = null ;
        histData = hedRepo.getHistoricData( symbolNse, fromDate, toDate ) ;

        histData.forEach( item -> {
            DataHolder holder = new DataHolder() ;
            holder.histData = item ;
            data.put( item.getDate(), holder ) ;
        } ) ;
        
        populateLatestCMP( data, symbolNse ) ;
        
        return data ;
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
                    holder.trades.add( trade ) ;
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
            if( !holding.trades.isEmpty() ) {
                holding.trades.forEach( graphData::addTrade ) ;
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
                float cmp = holding.getCurrentMktPrice() ;
                
                histData.setDate ( lastUpdate ) ;
                histData.setOpen ( cmp        ) ;
                histData.setHigh ( cmp        ) ;
                histData.setLow  ( cmp        ) ;
                histData.setClose( cmp        ) ;
                
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
