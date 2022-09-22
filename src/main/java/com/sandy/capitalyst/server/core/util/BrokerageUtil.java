package com.sandy.capitalyst.server.core.util;

public class BrokerageUtil {

    // Dirty. Move it to external configuration later on.
    public static float computeBrokerage( float tradePrice, String ownerName ) {
        
        float brokeragePct = 0.55f ;
        
        // This is dependent upon the brokerage plan.
        if( ownerName.equalsIgnoreCase( "Sandeep" ) ) {
            brokeragePct = (float)( tradePrice * (0.1/100)) ;
        }
        else if( ownerName.equalsIgnoreCase( "Sova" ) ) {
            brokeragePct = (float)( tradePrice * (0.22/100)) ;
        }

        return (float)( tradePrice * (brokeragePct/100)) ;
    }
}
