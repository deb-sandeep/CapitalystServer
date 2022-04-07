package com.sandy.capitalyst.server.api.ledger.helpers;

import lombok.Data ;

@Data
public class RuleMatchCount {
    private int ruleId = 0 ;
    private int numMatches = 0 ;
    
    public RuleMatchCount( int ruleId, int count ) {
        this.ruleId = ruleId ;
        this.numMatches = count ;
    }
}
