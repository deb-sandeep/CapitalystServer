package com.sandy.capitalyst.server.api.ledger.helpers.loadcalc;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.udojava.evalex.Expression ;

class LoadRuleCtx {
    
    static final Logger log = Logger.getLogger( LoadRuleCtx.class ) ;

    private Map<String, Integer> varMap = new HashMap<>() ;
    
    public void addVariable( String varName, String expr ) {
        
        int exprVal = evaluate( expr ) ;
        varMap.put( varName, exprVal ) ;
    }
    
    public int evaluate( String expr ) {
        
        Expression e = new Expression( expr ) ;
        for( String var : varMap.keySet() ) {
            Integer val = varMap.get( var ) ;
            e.setVariable( var, val.toString() ) ;
        }
        
        int val = e.eval( true ).intValue() ;
        return val ;
    }
}
