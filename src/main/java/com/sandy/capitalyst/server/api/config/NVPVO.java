package com.sandy.capitalyst.server.api.config;

import com.sandy.capitalyst.server.dao.nvp.NVP ;

import lombok.Data ;

@Data
public class NVPVO {

    private Integer id          = null ;
    private String  groupName   = null ;
    private String  configName  = null ;
    private String  value       = null ;
    private String  description = null ;
    private boolean boolFlag    = false ;
    
    public NVPVO() {}
    
    public NVPVO( NVP master ) {
        this.id          = master.getId() ;
        this.groupName   = master.getGroupName() ;
        this.configName  = master.getConfigName() ;
        this.value       = master.getValue() ;
        this.description = master.getDescription() ;
        this.boolFlag    = this.value != null && 
                           ( this.value.equalsIgnoreCase( "true" ) || 
                             this.value.equalsIgnoreCase( "false" ) ) ;
    }

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
