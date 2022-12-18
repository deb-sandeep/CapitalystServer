package com.sandy.capitalyst.server.dao.equity;

import java.util.Date ;

import javax.persistence.Column ;
import javax.persistence.Entity ;
import javax.persistence.GeneratedValue ;
import javax.persistence.GenerationType ;
import javax.persistence.Id ;
import javax.persistence.Table ;
import javax.persistence.TableGenerator ;

import com.sandy.capitalyst.server.dao.EntityWithNumericID ;

import lombok.Data ;

@Data
@Entity
@Table( name = "equity_indicators_hist" )
public class EquityIndicatorsHist implements EntityWithNumericID {

    @Id
    @TableGenerator(
        name            = "eihPkGen", 
        table           = "id_gen", 
        pkColumnName    = "gen_key", 
        valueColumnName = "gen_value", 
        pkColumnValue   = "equity_indicators_hist_id",
        initialValue    = 1,
        allocationSize  = 1 )    
    @GeneratedValue( 
        strategy=GenerationType.TABLE, 
        generator="eihPkGen" )
    private Integer id = null ;
    
    private String isin           = null ;
    private String symbolNse      = null ;
    private String sector         = null ;
    private float  beta           = 0 ;
    private float  high52         = 0 ;
    private float  low52          = 0 ;
    private float  eps            = 0 ;
    private float  pe             = 0 ;
    private float  pb             = 0 ;
    private float  dividendYeild  = 0 ;
    private float  cagrRevenue    = 0 ;
    private float  cagrNetProfit  = 0 ;
    private float  cagrEbit       = 0 ;    
    private int    marketCap      = 0 ;
    private int    piotroskiScore = 0 ;
    private float  currentPrice   = 0 ;
    private Date   asOnDate       = null ;
    
    private float sma5   = 0 ;
    private float sma10  = 0 ;
    private float sma20  = 0 ;
    private float sma50  = 0 ;
    private float sma100 = 0 ;
    private float sma200 = 0 ;
    
    @Column( name = "sector_pe" )
    private float sectorPE = 0 ;

    @Column( name = "price_perf_1d" )
    private Float pricePerf1D = null ;

    @Column( name = "price_perf_1w" )
    private float pricePerf1W = 0 ;

    @Column( name = "price_perf_1m" )
    private float pricePerf1M = 0 ;

    @Column( name = "price_perf_3m" )
    private float pricePerf3M = 0 ;

    @Column( name = "price_perf_1y" )
    private float pricePerf1Y = 0 ;

    @Column( name = "price_perf_3y" )
    private float pricePerf3Y = 0 ;

    @Column( name = "price_perf_ytd" )
    private float pricePerfYTD = 0 ;
    
    private String trend = null ;
    private String prevTrend = null ;
    private int    mcEssentialScore = 0 ;
    private String mcInsightShort = null ;
    private String mcInsightLong = null ;
    
    private int communitySentimentBuy  = 0 ;
    private int communitySentimentSell = 0 ;
    private int communitySentimentHold = 0 ;
    
    public EquityIndicatorsHist() {}
    
    public EquityIndicatorsHist( EquityIndicators ind ) {
        copy( ind ) ;
    }
    
    public void copy( EquityIndicators ind ) {
        
        this.isin                   = ind.getIsin() ;
        this.symbolNse              = ind.getSymbolNse() ;
        this.sector                 = ind.getSector() ;
        this.beta                   = ind.getBeta() ;
        this.high52                 = ind.getHigh52() ;
        this.low52                  = ind.getLow52() ;
        this.eps                    = ind.getEps() ;
        this.pe                     = ind.getPe() ;
        this.sectorPE               = ind.getSectorPE() ;
        this.pb                     = ind.getPb() ;
        this.dividendYeild          = ind.getDividendYeild() ;
        this.cagrRevenue            = ind.getCagrRevenue() ;
        this.cagrNetProfit          = ind.getCagrNetProfit() ;
        this.cagrEbit               = ind.getCagrEbit() ;
        this.marketCap              = ind.getMarketCap() ;
        this.piotroskiScore         = ind.getPiotroskiScore() ;
        this.asOnDate               = ind.getAsOnDate() ;
        this.currentPrice           = ind.getCurrentPrice() ;
        this.sma5                   = ind.getSma5() ;
        this.sma10                  = ind.getSma10() ;
        this.sma20                  = ind.getSma20() ;
        this.sma50                  = ind.getSma50() ;
        this.sma100                 = ind.getSma100() ;
        this.sma200                 = ind.getSma200() ;
        this.pricePerf1D            = ind.getPricePerf1D() ;
        this.pricePerf1W            = ind.getPricePerf1W() ;
        this.pricePerf1M            = ind.getPricePerf1M() ;
        this.pricePerf3M            = ind.getPricePerf3M() ;
        this.pricePerf1Y            = ind.getPricePerf1Y() ;
        this.pricePerf3Y            = ind.getPricePerf3Y() ;
        this.pricePerfYTD           = ind.getPricePerfYTD() ;
        this.trend                  = ind.getTrend() ;
        this.mcEssentialScore       = ind.getMcEssentialScore() ;
        this.mcInsightShort         = ind.getMcInsightShort() ;
        this.mcInsightLong          = ind.getMcInsightLong() ;
        this.communitySentimentBuy  = ind.getCommunitySentimentBuy() ;
        this.communitySentimentSell = ind.getCommunitySentimentSell() ;
        this.communitySentimentHold = ind.getCommunitySentimentHold() ;
    }
}