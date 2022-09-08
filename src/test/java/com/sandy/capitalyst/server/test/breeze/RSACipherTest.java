package com.sandy.capitalyst.server.test.breeze;

import com.sandy.capitalyst.server.core.util.RSACipher ;

public class RSACipherTest {

    public static void main(String[] args) throws Exception {
        //                    1         2         3         4
        //          12345678901234567890123456789012345678901234567890
        String N = "90755611487566208138950675092879865387596685014726" + 
                   "50153125015725848249547852476945622291384366563482" + 
                   "46840374688179808142310548561251271158941893857171" + 
                   "48934026931120932481402379431731629550862846041784" + 
                   "30527465147608689216580522371955257559996225339224" + 
                   "80798112680619461022349354227721314753409888828250" + 
                   "43233323" ;
        
        String d = "17790520481266507102264359414044396762660094486842" + 
                   "41520319774738391633152894712472655287508048235974" + 
                   "47657938166517326017429293641246854152294528440164" + 
                   "82477236658413327331659722342187036963943428678684" + 
                   "67727903226350101114388281472816021538005128750321" + 
                   "97327371978086111445077205212013931296929969265999" + 
                   "75297921" ;
        
        String e = "65537";

        byte[] input = "This is a test.".getBytes( "UTF-8" ) ;

        RSACipher enc = new RSACipher( N, e, 10 );
        RSACipher dec = new RSACipher( N, e, d, 10 );
        
        byte[] encData = enc.encrypt( input ) ;
        byte[] decData = dec.decrypt( encData ) ;

        System.out.println( new String( decData ) ) ;
    }
}
