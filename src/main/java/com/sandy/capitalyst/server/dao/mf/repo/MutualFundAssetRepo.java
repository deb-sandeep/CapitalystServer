package com.sandy.capitalyst.server.dao.mf.repo;

import java.util.List ;

import org.springframework.data.repository.CrudRepository ;

import com.sandy.capitalyst.server.dao.mf.MutualFundAsset ;

public interface MutualFundAssetRepo 
    extends CrudRepository<MutualFundAsset, Integer> {
    
    MutualFundAsset findByOwnerNameAndScheme( 
            String ownerName, 
            String scheme ) ;
    
    List<MutualFundAsset> findByUnitsHeldGreaterThanOrderByOwnerNameAsc( 
            float numUnits ) ;
}
