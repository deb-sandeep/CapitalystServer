package com.sandy.capitalyst.server.api.ledger.helpers;

import lombok.Data ;

@Data
public class SplitDetails {
    
    private int     entryId = 0 ;
    private int     amount = 0 ;
    private String  l1Cat = null ;
    private String  l2Cat = null ;
    private boolean newClassifier = false ;
    private String  notes = null ;
}
