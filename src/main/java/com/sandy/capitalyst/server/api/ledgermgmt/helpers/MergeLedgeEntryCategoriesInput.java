package com.sandy.capitalyst.server.api.ledgermgmt.helpers;

import lombok.Data ;

@Data
public class MergeLedgeEntryCategoriesInput {
    
    private int oldCatId = 0 ;
    private String newL1CatName = null ;
    private String newL2CatName = null ;
}
