package com.sandy.capitalyst.server.api.config;

import lombok.Data ;

@Data
public class NVPVO {

    private Integer id          = null ;
    private String  groupName   = null ;
    private String  configName  = null ;
    private String  value       = null ;
    private String  description = null ;

    public String toString() {
        StringBuilder builder = new StringBuilder( "NVPVO [\n" ) ; 
        builder.append( "   group = " + this.groupName + "\n" ) ;
        builder.append( "   name  = " + this.configName  + "\n" ) ;
        builder.append( "   value = " + this.value + "\n" ) ;
        builder.append( "   desc  = " + this.description + "\n" ) ;
        builder.append( "]" ) ;
        return builder.toString() ;
    }
}
