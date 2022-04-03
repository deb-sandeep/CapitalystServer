package com.sandy.capitalyst.server.api.ledger.helpers;

import java.util.Date ;

import org.apache.commons.lang.ArrayUtils ;
import org.apache.commons.lang.StringUtils ;

import lombok.Data ;

@Data
public class LedgerSearchCriteria {

    private int[] accountIds = null ;
    private Date startDate = null ;
    private Date endDate = null ;
    private Float minAmt = null ;
    private Float maxAmt = null ;
    private String customRule = null ;
    private boolean showOnlyUnclassified = false ;
    
    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerSearchCriteria [\n" ) ; 

        builder.append( "   accountId = " + StringUtils.join( ArrayUtils.toObject( this.accountIds ), ",") + "\n" ) ;
        builder.append( "   startDate = " + this.startDate + "\n" ) ;
        builder.append( "   endDate = " + this.endDate + "\n" ) ;
        builder.append( "   minAmt = " + this.minAmt + "\n" ) ;
        builder.append( "   maxAmt = " + this.maxAmt + "\n" ) ;
        builder.append( "   customRule = " + this.customRule + "\n" ) ;
        builder.append( "   showOnlyUnclassified = " + this.showOnlyUnclassified + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}