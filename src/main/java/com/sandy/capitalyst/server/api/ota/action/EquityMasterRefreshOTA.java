package com.sandy.capitalyst.server.api.ota.action;

public class EquityMasterRefreshOTA extends OTA {

    public EquityMasterRefreshOTA() {
    }

    @Override
    public void run() {
        
        for( int i=0; i<15; i++ ) {
            try {
                Thread.sleep( 1000 ) ;
                super.addResult( "Count = " + i ) ;
            }
            catch( InterruptedException e ) {
                e.printStackTrace() ;
                super.addResult( e ) ;
            }
        }
        super.markEndOfProcessing() ;
    }
}
