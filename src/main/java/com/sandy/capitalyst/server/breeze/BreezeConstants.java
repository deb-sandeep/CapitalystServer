package com.sandy.capitalyst.server.breeze;

public class BreezeConstants {
    
    public static enum Action {
        
        BUY( "buy" ),
        SELL( "sell" );
        
        private final String value ;
        
        private Action( String value ) {
            this.value = value ;
        }
        
        public String toString() {
            return this.value ;
        }
    }

    public static enum ExchangeCode {
        
        NSE,
        BSE,
        NFO;
    }

    public static enum ProductType {
        
        FUTURES         ( "futures"         ),
        OPTIONS         ( "options"         ),
        FURTUREPLUS     ( "furtureplus"     ),
        FUTUREPLUS_SLTP ( "futureplus_sltp" ),
        OPTIONPLUS      ( "optionplus"      ),
        CASH            ( "cash"            ),
        EATM            ( "eatm"            ),
        BTST            ( "btst"            ),
        MARGIN          ( "margin"          ),
        MARGINPLUS      ( "marginplus"      );
        
        private final String value ;
        
        private ProductType( String value ) {
            this.value = value ;
        }
        
        public String toString() {
            return this.value ;
        }
    }
}
