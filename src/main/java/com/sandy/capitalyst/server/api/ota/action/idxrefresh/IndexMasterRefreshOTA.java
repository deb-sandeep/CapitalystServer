package com.sandy.capitalyst.server.api.ota.action.idxrefresh;

import com.sandy.capitalyst.server.api.ota.action.OTA ;

// Make repo
// Create mapping and repo - ref integrity
// Create historic table and repo - ref integrity
// Load cache
// Download latest index historic data
// Add a row if it doesn't exist
// Purge a row if it exists - including mapping and historic data
// Refresh mapping
// For the indexes in DB
// load URL
// Refresh mapping

public class IndexMasterRefreshOTA extends OTA {
    
    public static final String NAME = "RefreshIndexMaster" ;
    
    public IndexMasterRefreshOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
    }
}
