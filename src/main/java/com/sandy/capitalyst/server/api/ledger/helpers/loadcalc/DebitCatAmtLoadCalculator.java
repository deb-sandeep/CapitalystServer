package com.sandy.capitalyst.server.api.ledger.helpers.loadcalc;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.capitalyst.server.api.ledger.helpers.loadcalc.LoadRule.LoadAction ;
import com.sandy.common.util.StringUtil ;
import com.udojava.evalex.Expression.ExpressionException ;

import lombok.Getter ;

/**
 * 
 * var amt : 12000
 * Jan-Dec : amt/12
 * Mar,Jun,Sep,Dec : amt/24 
 * 
 * Jan : 1000
 * Feb : 1000
 * Mar : 1000 + 500
 * Apr : 1000
 * May : 1000
 * Jun : 1000 + 500
 * Jul : 1000
 * Aug : 1000
 * Sep : 1000 + 500
 * Oct : 1000
 * Nov : 1000
 * Dec : 1000 + 500
 *
 */
public class DebitCatAmtLoadCalculator {
    
    private static final Logger log = Logger.getLogger( DebitCatAmtLoadCalculator.class ) ;
    
    private LoadRuleCtx ctx = new LoadRuleCtx() ;
    private List<LoadRule> rules = new ArrayList<>() ;
    
    @Getter private int[] monthlyCap = new int[12] ;
    @Getter private int yearlyCap = 0 ;

    public DebitCatAmtLoadCalculator( String ruleSet ) {
        parseRuleSet( ruleSet ) ;
        aggregateLoading() ;
    }
    
    private void raiseException( String msg ) {
        throw new IllegalArgumentException( msg ) ;
    }
    
    private void raiseException( int lineNo, String msg ) {
        throw new IllegalArgumentException( "Line " + lineNo + ". " + msg ) ;
    }
    
    private void parseRuleSet( String ruleSet )
        throws IllegalArgumentException {
        
        if( StringUtil.isEmptyOrNull( ruleSet ) ) {
            raiseException( "Empty ruleset" ) ;
        }
        
        String[] ruleStrings = ruleSet.split( "\\R" ) ;
        
        for( int i=0; i<ruleStrings.length; i++ ) {
            
            int lineNo = i+1 ;
            String str = ruleStrings[i].trim() ;
            
            if( StringUtil.isEmptyOrNull( str ) || 
                str.startsWith( "//" ) ) {
                continue ;
            }
            
            try {
                if( str.startsWith( "var" ) ) {
                    populateContext( lineNo, str ) ;
                }
                else {
                    rules.add( new LoadRule( lineNo, ctx, str ) ) ;
                }
            }
            catch( ExpressionException e ) {
                raiseException( lineNo, "Invalid expression - " + e.getMessage() ) ;
            }
        }
    }
    
    private void populateContext( int lineNo, String varDecl ) {
        
        // var <variableName> = <expr>
        String[] parts = varDecl.split( "=" ) ;
        if( parts.length != 2 ) {
            raiseException( lineNo, "Doesn't follow var decl syntax." ) ;
        }
        
        String var  = parts[0].trim() ;
        String expr = parts[1].trim() ;
        
        String[] varParts = var.split( "\\s+" ) ;
        if( varParts.length != 2 ) {
            raiseException( lineNo, "Doesn't follow var decl syntax." ) ;
        }
        
        String varName = varParts[1] ;
        ctx.addVariable( varName, expr ) ;
    }
    
    private void aggregateLoading() {
        
        for( LoadRule rule : rules ) {
            
            LoadAction loadAction = rule.getLoadAction() ;
            int[] mthLoads = rule.getMonthLoads() ;
            
            for( int i=0; i<mthLoads.length; i++ ) {
                switch( loadAction ) {
                    case REPLACE:
                        monthlyCap[i] = mthLoads[i] ;
                        break ;
                    case ADD:
                        monthlyCap[i] += mthLoads[i] ;
                        break ;
                    case SUBTRACT:
                        monthlyCap[i] -= mthLoads[i] ;
                        break ;
                }
            }
        }
        
        for( int i=0; i < monthlyCap.length; i++     ) {
            this.yearlyCap += monthlyCap[i] ;
        }
    }

    public static void main( String[] args ) {
        String ruleSet = 
                "var a = 15000\n" + 
                "var b = a*2\n" +
                "var c = (a^2 + b)/2\n" +
                "\n" + 
                "Jan-Dec = a/12\n" ;
        
        log.debug( "Rule set = \n" + ruleSet ) ;
        log.debug( "---------------------------------" ) ;
        
        DebitCatAmtLoadCalculator calc = null ;
        calc = new DebitCatAmtLoadCalculator( ruleSet ) ;
        
        int[] amt = calc.getMonthlyCap() ;
        for( int i=0; i<amt.length; i++ ) {
            log.debug( CalendarUtil.getMonthName( i ) + " = " + amt[i] );
        }
        
        log.debug( "\nYearly cap = " + calc.getYearlyCap() );
    }

}
