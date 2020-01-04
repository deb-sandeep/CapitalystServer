package com.sandy.capitalyst.server.dao.mf;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

public interface MutualFundAssetRepo 
    extends CrudRepository<MutualFundAsset, Integer> {
    
    MutualFundAsset findByOwnerNameAndScheme( 
            String ownerName, 
            String scheme ) ;
    
    List<MutualFundAsset> findByUnitsHeldGreaterThanOrderByOwnerNameAsc( 
            float numUnits ) ;
}
