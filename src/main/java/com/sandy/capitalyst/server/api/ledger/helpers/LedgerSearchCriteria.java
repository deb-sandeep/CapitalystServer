package com.sandy.capitalyst.server.api.ledger.helpers;

import java.util.Date ;

import org.apache.commons.lang.ArrayUtils ;
import org.apache.commons.lang.StringUtils ;

public class LedgerSearchCriteria {

    private int[] accountIds = null ;
    private Date startDate = null ;
    private Date endDate = null ;
    private Float minAmt = null ;
    private Float maxAmt = null ;
    private String customRule = null ;
    private boolean showOnlyUnclassified = false ;
    
    public LedgerSearchCriteria() {}

    public void setAccountIds( int[] val ) {
        this.accountIds = val ;
    }
        
    public int[] getAccountIds() {
        return this.accountIds ;
    }

    public void setStartDate( Date val ) {
        this.startDate = val ;
    }
        
    public Date getStartDate() {
        return this.startDate ;
    }

    public void setEndDate( Date val ) {
        this.endDate = val ;
    }
        
    public Date getEndDate() {
        return this.endDate ;
    }

    public Float getMinAmt() {
        return minAmt ;
    }

    public void setMinAmt( Float minAmt ) {
        this.minAmt = minAmt ;
    }

    public Float getMaxAmt() {
        return maxAmt ;
    }

    public void setMaxAmt( Float maxAmt ) {
        this.maxAmt = maxAmt ;
    }

    public String getCustomRule() {
        return customRule ;
    }

    public void setCustomRule( String customRule ) {
        this.customRule = customRule ;
    }

    public boolean isShowOnlyUnclassified() {
        return showOnlyUnclassified ;
    }

    public void setShowOnlyUnclassified( boolean bool ) {
        this.showOnlyUnclassified = bool ;
    }

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