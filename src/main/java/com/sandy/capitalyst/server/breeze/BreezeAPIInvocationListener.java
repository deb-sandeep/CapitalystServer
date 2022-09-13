package com.sandy.capitalyst.server.breeze;

import java.util.Date ;

import lombok.Data ;

public interface BreezeAPIInvocationListener {

    @Data
    public static class APIInvocationInfo {
        
        // Invocation id a unique identifier which can be used to correlate
        // data between callbacks
        private String invocationId = null ;
        private String apiId = null ;
        private String userName = null ;
        private Date   callDatetime = null ;
        private int    callDurationInMillis = 0 ;
        private int    callStatus = 0 ;
        private String errorMsg = null ;
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder() ;
            builder.append( "APIInvocationInfo [" )
                   .append( "\n  invocationId = "         ).append( invocationId         )
                   .append( "\n  apiId = "                ).append( apiId                )
                   .append( "\n  userName = "             ).append( userName             )
                   .append( "\n  callDatetime = "         ).append( callDatetime         )
                   .append( "\n  callDurationInMillis = " ).append( callDurationInMillis )
                   .append( "\n  callStatus = "           ).append( callStatus           )
                   .append( "\n  errorMsg = "             ).append( errorMsg             )
                   .append( "\n]" ) ;
            return builder.toString() ;
        }
    }
    
    public void preBreezeCall( APIInvocationInfo info ) ;
    
    public void postBreezeCall( APIInvocationInfo info ) ;
}
