package com.sandy.capitalyst.server.api.ledger;

import java.util.Date ;

public class LedgerSearchCriteria {

    private Integer accountId = null ;
    private Date startDate = null ;
    private Date endDate = null ;
    private Float amtThreshold = null ;

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

    public void setAmtThreshold( Float val ) {
        this.amtThreshold = val ;
    }
        
    public Float getAmtThreshold() {
        return this.amtThreshold ;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder( "LedgerSearchCriteria [\n" ) ; 

        builder.append( "   accountId = " + this.accountId + "\n" ) ;
        builder.append( "   startDate = " + this.startDate + "\n" ) ;
        builder.append( "   endDate = " + this.endDate + "\n" ) ;
        builder.append( "   amtThreshold = " + this.amtThreshold + "\n" ) ;
        builder.append( "]" ) ;
        
        return builder.toString() ;
    }
}