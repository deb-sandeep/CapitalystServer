package com.sandy.capitalyst.server.core.util;

public class BrokerageUtil {

    // Dirty. Move it to external configuration later on.
    public static float computeBrokerage( float tradePrice, String ownerName ) {
        
        float brokeragePct = 0.55f ;
        
        // This is dependent upon the brokerage plan.
        if( ownerName.equalsIgnoreCase( "Sandeep" ) ) {
            brokeragePct = 0.1f ;
        }
        else if( ownerName.equalsIgnoreCase( "Sova" ) ) {
            brokeragePct = 0.22f ;
        }

        return (float)( tradePrice * (brokeragePct/100)) ;
    }
}
