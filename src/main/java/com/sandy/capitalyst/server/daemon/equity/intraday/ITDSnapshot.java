package com.sandy.capitalyst.server.daemon.equity.intraday;

import java.util.Date ;

import lombok.Data ;

@Data
public class ITDSnapshot {

    private String   symbol            = null ; 
    private String   isin              = null ;
    private double   open              = 0    ; 
    private double   dayHigh           = 0    ; 
    private double   dayLow            = 0    ; 
    private double   lastPrice         = 0    ; 
    private double   change            = 0    ; 
    private double   pChange           = 0    ; 
    private double   totalTradedVolume = 0    ; 
    private double   totalTradedValue  = 0    ; 
    private Date     lastUpdateTime    = null ; 
    private boolean  isETFSec          = false; 
}
