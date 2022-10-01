package com.sandy.capitalyst.server.api.equity.vo;

import java.util.ArrayList ;
import java.util.List ;

import lombok.Data ;

@Data
public class GraphData {
    
    @Data
    public static class TradeData {
        private Long x ;
        private Float y ;
        private Integer q ;
    }

    @Data
    public static class AvgCostData {
        private Long x ;
        private Float y ;
    }

    private List<Long>        labels       = new ArrayList<>() ;
    private List<Float>       eodPriceList = new ArrayList<>() ;
    private List<TradeData>   buyData      = new ArrayList<>() ;
    private List<TradeData>   sellData     = new ArrayList<>() ;
    private List<AvgCostData> avgData      = new ArrayList<>() ;
}
