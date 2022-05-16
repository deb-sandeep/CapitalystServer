package com.sandy.capitalyst.server.api.ota.action.idxrefresh;

import static com.sandy.capitalyst.server.CapitalystServer.getBean ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.index.helper.IndexRefresher ;
import com.sandy.capitalyst.server.api.ota.action.OTA ;
import com.sandy.capitalyst.server.dao.index.IndexMaster ;
import com.sandy.capitalyst.server.dao.index.repo.IndexMasterRepo ;

public class IndexMasterRefreshOTA extends OTA {
    
    private static final Logger log = Logger.getLogger( IndexMasterRefreshOTA.class ) ;
    
    public static final String NAME = "RefreshIndexMaster" ;
    
    public IndexMasterRefreshOTA() {
        super( NAME ) ;
    }

    @Override
    protected void execute() throws Exception {
        
        IndexMasterRepo imRepo = getBean( IndexMasterRepo.class ) ;
        Iterable<IndexMaster> indexMasters = imRepo.findAll() ;
        
        IndexRefresher refresher = new IndexRefresher(this ) ;
        
        for( IndexMaster idx : indexMasters ) {
            try {
                refresher.refreshIndex( idx ) ;
            }
            catch( Exception e ) {
                log.error( "Error refreshing index " + idx.getName(), e ) ;
                super.addResult( e ) ;
            }
        }
    }
}
