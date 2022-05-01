package com.sandy.capitalyst.server.api.ota.vo;

import lombok.Data ;

@Data
public class PartResult {

    public static enum ResultType { Message, Exception, EndOfProcessing } ;
    
    private ResultType resultType = null ;
    private String message = null ;
    
    public PartResult( ResultType type, String message ) {
        this.resultType = type ;
        this.message = message ;
    }
}
