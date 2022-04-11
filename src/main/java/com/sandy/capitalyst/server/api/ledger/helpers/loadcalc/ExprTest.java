package com.sandy.capitalyst.server.api.ledger.helpers.loadcalc;

import com.udojava.evalex.Expression ;

public class ExprTest {

    public static void main( String[] args ) {
        
        Expression expr = new Expression( "yearCap/12 + 500" ) ;
        expr.setVariable( "yearCap", "12000" ) ;
        
        System.out.println( expr.eval().intValue() ) ;
    }

}
