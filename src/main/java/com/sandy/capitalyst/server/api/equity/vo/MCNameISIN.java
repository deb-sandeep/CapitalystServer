package com.sandy.capitalyst.server.api.equity.vo;

import java.io.Serializable ;

import lombok.Data ;

@Data
public class MCNameISIN implements Serializable {

    private static final long serialVersionUID = 2244901068311057725L ;
    
    private String mcName = null ;
    private String isin = null ;
    private String detailURL = null ;
}
