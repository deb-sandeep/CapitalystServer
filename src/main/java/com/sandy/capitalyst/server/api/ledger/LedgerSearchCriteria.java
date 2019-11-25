package com.sandy.capitalyst.server.api.ledger;

import java.util.Date ;

public class LedgerSearchCriteria {

    private Integer accountId = null ;
    private Date startDate = null ;
    private Date endDate = null ;
    private Float lowerAmtThreshold = null ;
    private Float upperAmtThreshold = null ;

    public LedgerSearchCriteria() {}

    public void setAccountId( Integer val ) {
        this.accountId = val ;
    }
        
    public Integer getAccountId() {
        return this.accountId ;
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

    public Float getLowerAmtThreshold() {
        return lowerAmtThreshold ;
    }

    public void setLowerAmtThreshold( Float amt ) {
        this.lowerAmtThreshold = amt ;
    }

    public Float getUpperAmtThreshold() {
        return upperAmtThreshold ;
    }

    public void setUpperAmtThreshold( Float amt ) {
        this.upperAmtThreshold = amt ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerSearchCriteria [\n" ) ; 

        builder.append( "   accountId = " + this.accountId + "\n" ) ;
        builder.append( "   startDate = " + this.startDate + "\n" ) ;
        builder.append( "   endDate = " + this.endDate + "\n" ) ;
        builder.append( "   lowerAmtThreshold = " + this.lowerAmtThreshold + "\n" ) ;
        builder.append( "   upperAmtThreshold = " + this.upperAmtThreshold + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}